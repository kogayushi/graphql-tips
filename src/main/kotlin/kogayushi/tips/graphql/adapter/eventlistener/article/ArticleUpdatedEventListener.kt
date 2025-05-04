package kogayushi.tips.graphql.adapter.eventlistener.article

import kogayushi.tips.graphql.model.article.ArticleUpdatedEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ArticleUpdatedEventListener(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {

    // コミット後に非同期で実行する
    // そうすることで、エラーが発生してトランザクションがロールバックされたのに、イベントを通知してしまうことを防ぐ
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ArticleUpdatedEvent) {
        kafkaTemplate.send(
            "article-updated",
            event.id.toString(),
            event
        )
    }
}
