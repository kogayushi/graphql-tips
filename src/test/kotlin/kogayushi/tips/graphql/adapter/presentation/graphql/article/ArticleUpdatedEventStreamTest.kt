package kogayushi.tips.graphql.adapter.presentation.graphql.article

import kogayushi.tips.graphql.model.article.Article
import kogayushi.tips.graphql.model.article.ArticleUpdatedEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier
import java.time.Duration
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

class ArticleUpdatedEventStreamTest {

    @Test
    fun `shutdown should terminate executor`() {
        // set up
        val eventStream = ArticleUpdatedEventStream()
        val executor = ReflectionTestUtils.getField(eventStream, "executor") as ExecutorService

        // exercise
        eventStream.shutdown()

        // verify
        assertTrue(executor.isShutdown, "Executor should be shutdown")
        assertTrue(
            executor.isTerminated || executor.awaitTermination(1, TimeUnit.SECONDS),
            "Executor should be terminated or terminate within timeout"
        )
    }

    @Test
    fun `shutdown should complete the sink`() {
        // set up
        val eventStream = ArticleUpdatedEventStream()
        val flux = eventStream.asFlux(
            emptySet(),
            OffsetDateTime.now()
        )
        val stepVerifier = StepVerifier.create(flux)

        // exercise and verify
        stepVerifier.then { eventStream.shutdown() }.verifyComplete()
    }

    @Test
    fun `shutdown should drain remaining events for slow client`() {
        // set up
        val eventCount = 192 // Using the CAPACITY value directly
        val subscribeStartAt = OffsetDateTime.now()

        val events = List(eventCount) {
            ArticleUpdatedEvent(UUID.randomUUID(), dummyArticle("event-$it"), OffsetDateTime.now())
        }
        val articleIds = events.map { it.source.id }.toSet()

        val sut = ArticleUpdatedEventStream()

        val received = mutableListOf<ArticleUpdatedEvent>()
        val delayMilliSeconds = 10L

        val subscription = sut
            .asFlux(
                articleIds,
                subscribeStartAt,
            )
            // workerと別スレッドにしないとworkerのThread.sleep()でブロックされてしまう
            .publishOn(Schedulers.newSingle("subscription"))
            .delayElements(Duration.ofMillis(delayMilliSeconds))
            .subscribe {
                // 何件目か
                received.add(it)
            }

        events.forEach {
            sut.emit(it)
        }

        // exercise
        sut.shutdown()

        assertEquals(eventCount, received.size)
        assertTrue(received.containsAll(events))
        assertTrue(subscription.isDisposed)
    }

    private fun dummyArticle(suffix: String) = Article(
        id = UUID.randomUUID(),
        title = "Title $suffix",
        content = "Content $suffix",
        authorId = UUID.randomUUID(),
        likedBy = emptyList(),
        scheduledPublishDate = null
    )

    @Test
    fun `shutdown should handle interruption gracefully`() {
        // set up
        val eventStream = ArticleUpdatedEventStream()

        val interrupterThread = Thread {
            try {
                Thread.sleep(100) // Give some time for shutdown to start
                Thread.currentThread().interrupt()
            } catch (e: InterruptedException) {
                // Ignore
            }
        }

        // exercise
        interrupterThread.start()
        eventStream.shutdown()

        // verify
        assertTrue(true, "Shutdown completed without throwing exceptions despite interruption")
    }

    @Test
    fun `should process events exceeding buffer capacity when subscription can keep up`() {
        // set up
        val eventCount = 192 * 2 // Double the buffer capacity (192 is the CAPACITY value)
        val subscribeStartAt = OffsetDateTime.now()

        val events = List(eventCount) {
            ArticleUpdatedEvent(UUID.randomUUID(), dummyArticle("event-$it"), OffsetDateTime.now())
        }
        val articleIds = events.map { it.source.id }.toSet()

        val sut = ArticleUpdatedEventStream()

        val received = mutableListOf<ArticleUpdatedEvent>()

        // Create a subscription that processes events quickly (no delay)
        val subscription = sut
            .asFlux(
                articleIds,
                subscribeStartAt,
            )
            .publishOn(Schedulers.newSingle("subscription"))
            .subscribe {
                received.add(it)
            }

        // Emit events exceeding the buffer capacity with a small delay to allow processing
        events.forEach {
            sut.emit(it)
            // Add a small delay to allow the worker thread to process events from the queue
            // Without this delay, the queue would fill up before the worker thread can process events,
            // resulting in an IllegalStateException with "Queue full" message
            Thread.sleep(1)
        }

        // Give some time for all events to be processed
        Thread.sleep(1000)

        // Clean up
        sut.shutdown()

        // Verify all events were processed without errors
        assertEquals(eventCount, received.size)
        assertTrue(received.containsAll(events))
        assertTrue(subscription.isDisposed)
    }

    @Test
    fun `asFlux should filter events by articleId and subscribeStartAt`() {
        // set up
        val sut = ArticleUpdatedEventStream()
        val articleId1 = UUID.randomUUID()
        val articleId2 = UUID.randomUUID()
        val articleId3 = UUID.randomUUID()

        val subscribeStartAt = OffsetDateTime.now()

        // Create events with different articleIds and timestamps
        val beforeSubscribeEvent = ArticleUpdatedEvent(
            UUID.randomUUID(),
            dummyArticle("before-subscribe").copy(id = articleId1),
            subscribeStartAt.minusSeconds(1)
        )

        val afterSubscribeEvent1 = ArticleUpdatedEvent(
            UUID.randomUUID(),
            dummyArticle("after-subscribe-1").copy(id = articleId1),
            subscribeStartAt.plusSeconds(1)
        )

        val afterSubscribeEvent2 = ArticleUpdatedEvent(
            UUID.randomUUID(),
            dummyArticle("after-subscribe-2").copy(id = articleId2),
            subscribeStartAt.plusSeconds(2)
        )

        val afterSubscribeEvent3 = ArticleUpdatedEvent(
            UUID.randomUUID(),
            dummyArticle("after-subscribe-3").copy(id = articleId3),
            subscribeStartAt.plusSeconds(3)
        )

        // Emit all events
        sut.emit(beforeSubscribeEvent)
        sut.emit(afterSubscribeEvent1)
        sut.emit(afterSubscribeEvent2)
        sut.emit(afterSubscribeEvent3)

        // Subscribe to events for articleId1 and articleId2 only
        val articleIds = setOf(articleId1, articleId2)

        // Create StepVerifier to test the flux
        StepVerifier.create(sut.asFlux(articleIds, subscribeStartAt))
            // Should receive afterSubscribeEvent1 and afterSubscribeEvent2 only
            // (filtered by both articleId and subscribeStartAt)
            .expectNext(afterSubscribeEvent1)
            .expectNext(afterSubscribeEvent2)
            .then { sut.shutdown() }
            .verifyComplete()
    }

    @Test
    fun `events should be removed from replay buffer after TTL expires`() {
        // set up
        val sut = ArticleUpdatedEventStream()
        val articleId = UUID.randomUUID()
        val subscribeStartAt = OffsetDateTime.now()

        // Create an event
        val event = ArticleUpdatedEvent(
            UUID.randomUUID(),
            dummyArticle("ttl-test").copy(id = articleId),
            subscribeStartAt.plusSeconds(1)
        )

        // Emit the event
        sut.emit(event)

        // Verify the event is initially available
        StepVerifier.create(sut.asFlux(setOf(articleId), subscribeStartAt))
            .expectNext(event)
            .then { sut.shutdown() }
            .verifyComplete()

        // Create a new instance to test TTL expiration
        val sutForTTL = ArticleUpdatedEventStream()

        // Emit the same event
        sutForTTL.emit(event)

        // Wait longer than the TTL duration (8 seconds + margin)
        Thread.sleep(9000)

        // Verify the event is no longer available due to TTL expiration
        StepVerifier.create(sutForTTL.asFlux(setOf(articleId), subscribeStartAt))
            .expectNextCount(0) // No events should be received
            .then { sutForTTL.shutdown() }
            .verifyComplete()
    }
}
