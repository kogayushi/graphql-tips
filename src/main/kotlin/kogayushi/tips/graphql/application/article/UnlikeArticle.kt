package kogayushi.tips.graphql.application.article

import kogayushi.tips.graphql.model.article.ArticleRepository
import kogayushi.tips.graphql.model.article.ArticleUpdatedEvent
import kogayushi.tips.graphql.model.fundamental.DomainEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class UnlikeArticleInputData(
    val articleId: UUID,
    val userId: UUID
)

@Service
class UnlikeArticle(
    private val articleRepository: ArticleRepository,
    private val domainEventPublisher: DomainEventPublisher,
) {
    // DBは使ってないが、TransactionalEventListenerの例を示すためにTransactionalをつけている
    @Transactional
    fun handle(input: UnlikeArticleInputData) {

        val article = articleRepository.resolveByArticleId(input.articleId)
            ?: throw IllegalArgumentException("Article not found")
        val unliked = article.unlikedBy(input.userId)
        articleRepository.update(unliked)

        val event = ArticleUpdatedEvent(
            id = UUID.randomUUID(),
            source = unliked,
            occurredAt = java.time.OffsetDateTime.now()
        )
        domainEventPublisher.publish(event)
    }
}
