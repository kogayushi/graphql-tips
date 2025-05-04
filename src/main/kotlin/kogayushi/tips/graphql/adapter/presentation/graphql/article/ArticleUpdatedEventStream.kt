package kogayushi.tips.graphql.adapter.presentation.graphql.article

import jakarta.annotation.PreDestroy
import kogayushi.tips.graphql.model.article.ArticleUpdatedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import reactor.core.publisher.Sinks.Many
import java.time.Duration
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

@Service
class ArticleUpdatedEventStream {

    /**
     * 購読開始後のイベントのみをリアルタイムに配信する Sink。
     *
     * replay によりCAPACITYまでのイベントをTTLが経過するまでの間、一時的にバッファできる。メモリ消費量を最適化するため、上限は1インスタンスあたりに要求する性能に応じて調整する。
     */
    private val sink: Many<ArticleUpdatedEvent> = Sinks.many().replay().limit(CAPACITY, TTL)

    /**
     * emitされたイベントを保持するキュー。複数スレッドから同時にemit可能にするために使用。また、Queueが詰まったことを検知できるように上限を設けている。
     * - 処理能力を超えたスループットでイベントが流入した場合、add() によって例外がスローされ、問題の早期検知（過剰負荷や設計バグ）を可能にしている。
     * - LinkedBlockingQueue は add（enqueue）と take（dequeue）で内部的に別ロックを使用しており、プロデューサとコンシューマが並行に動作してもブロックしにくく、高スループットが期待できる（はず）。
     */
    private val queue: BlockingQueue<ArticleUpdatedEvent> = LinkedBlockingQueue(CAPACITY)

    /**
     * ワーカースレッドを生成・保持する ExecutorService
     * メンバ変数にしているのは、シャットダウン時に executor にアクセスして明示的に shutdownNow() を呼び、
     * ワーカースレッドの停止とリソース解放を安全に行うため。
     */
    private val executor = Executors.newSingleThreadExecutor()

    init {
        // ワーカースレッドを起動して、キューからイベントを順に Sink に流す処理を開始する
        executor.submit(DrainQueuedEventToSinkWorker())
    }

    fun emit(event: ArticleUpdatedEvent) {
        try {
            // キューに空きがない場合は IllegalStateException をスローし、即時にログ出力。
            // サイレント障害対策、開発・運用でボトルネックの早期検知が可能。
            queue.add(event)
        } catch (e: IllegalStateException) {
            // キューが満杯のためイベントを取りこぼした。
            // 運用でアラートを検知できるようエラーログを出力して明示的に記録する。
            log.error("Emit queue full. Event dropped: {}", event, e)
        }
    }

    fun asFlux(articleId: Set<UUID>, subscribeStartAt: OffsetDateTime): Flux<ArticleUpdatedEvent> {
        return sink
            .asFlux()
            // 購読者が関心のある記事IDに関するイベントのみをフィルタ
            .filter { articleId.contains(it.source.id) }
            // 購読開始前に発生したイベントを除外することで、リアルタイム性を担保
            // （Sinkのreplay特性によりTTL分の履歴しか保持されないが、不要な履歴は受け取らない）
            .filter { it.occurredAt.isAfter(subscribeStartAt) }
    }

    @PreDestroy
    fun shutdown() {
        // ワーカースレッドに割り込みを送り、take() を中断させる
        log.info("starting Shutdown event stream worker.")
        executor.shutdownNow()
        log.info("started Shutdown event stream worker.")
        try {
            // ワーカースレッドの終了（＝Queue内の未処理イベントのemit完了）を待機する。
            //
            // 実行環境が SIGTERM を送ってからプロセスを強制終了（SIGKILL）するまでの猶予時間
            // に収まるよう、この待機時間を調整する必要がある。
            //
            // 処理中のイベントを確実に配信しきるためには、環境ごとの猶予内に この待機処理が完了することが望ましい。
            // この例のままだと、ローカル環境では再起動に時間がかかってしまうので環境ごとに設定できるようにしたほうが良い。
            val gracePeriodMilliSecond = 30L * 1000L
            // 全体のgrace period（例えば、AWS ECSなら30秒）を考慮し、
            // ここではその半分のみをイベント排出の完了待ちに使用する。
            // Springのライフサイクルや他の PreDestroy 処理との協調を意識する必要がある。
            log.info("Waiting for event stream worker to shut down. gracePeriod = {}ms", gracePeriodMilliSecond)
            Thread.sleep(gracePeriodMilliSecond / 2L)
            log.info("Shutdown event stream worker completed.")
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            log.warn("Interrupted while waiting for event stream worker to shut down.")
        }

        // ワーカーが残りを処理し終えた後で、購読者に complete シグナルを通知する
        sink.tryEmitComplete()
    }

    /**
     * イベントキューを監視し、順番に Sink に流すワーカースレッドの本体。
     * shutdownNow() による割り込み時は、Queue に残っていた未処理イベントも最後まで emit する。
     */
    private inner class DrainQueuedEventToSinkWorker : Runnable {
        override fun run() {
            // val event = queue.take()とval result = sink.tryEmitNext(event)の間で中断された場合、
            // queue.take()で取得したイベントは、sink.tryEmitNext()で処理されないまま残るため、
            // スコープを広げて保持しておくことで、InterruptedExceptionが発生した場合でも、ロストしないようにする。
            var event: ArticleUpdatedEvent? = null
            try {
                while (true) {
                    event = queue.take()
                    val result = sink.tryEmitNext(event)
                    if (result.isFailure) {
                        log.error("Failed to emit event: {}, {}", event, result)
                    }
                    @Suppress("UNUSED_VALUE")
                    event = null
                }
            } catch (e: InterruptedException) {

                log.info("Interrupted while draining event queue to sink.")
                Thread.currentThread().interrupt()
                // QoSのat-least-onceを目指す設計にしている。
                // InterruptedException発生時に「処理済みかもしれないイベント」を再送する可能性があるが、
                // 少なくともロストは避けたいというポリシーに基づく。
                // → 完全な exactly-once 処理は本クラス単体では保証しない。
                log.info("will drain remaining events in the queue to sink.")
                var drainingEvent = event ?: queue.poll()
                while (drainingEvent != null) {
                    sink.tryEmitNext(drainingEvent)
                    drainingEvent = queue.poll()
                }
                log.info("drained remaining events in the queue to sink.")
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ArticleUpdatedEventStream::class.java)

        /** 国外の低帯域ネットワーク（例：2G回線・日本⇔ブラジル間）において、WebSocket再接続後にイベント取りこぼしが発生しないよう、最大で5秒の伝送・接続復旧遅延を許容したTTLを設定。
         *
         *   - 想定遅延：伝送0.06s + 伝播0.13s + 再接続/処理遅延3〜5s = 最大約5s
         *   - 安全マージン50%を加算し、TTL = 7.5s → 切り上げて8秒とした。
         */
        private val TTL = Duration.ofSeconds(8L)
        /**
         *  想定条件：
         *   - 最大スループット：20イベント/秒
         *   - 最大想定遅延（クライアント再接続・処理遅延等）：8秒
         *   - 安全マージン：20%
         *
         * 計算：20 × 8 × 1.2 = 192件
         *
         * 意図：
         *
         *   メモリ過剰使用を防ぎつつ、クライアントの遅延に対応できるよう、Sinkのreplayバッファに十分な履歴を保持する
         */
        const val CAPACITY = 192
    }
}
