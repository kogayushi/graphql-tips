package kogayushi.tips.graphql.adapter.presentation.graphql.article

import kogayushi.tips.graphql.model.article.ArticleUpdatedEvent
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class ArticleUpdatedEventConsumer(
    private val articleUpdatedEventStream: ArticleUpdatedEventStream,
) {

    @KafkaListener(topics = ["article-updated"])
    fun consume(@Payload event: ArticleUpdatedEvent) {
        articleUpdatedEventStream.emit(event)
    }
}
