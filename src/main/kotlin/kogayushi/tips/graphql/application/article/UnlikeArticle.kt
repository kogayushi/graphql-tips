package kogayushi.tips.graphql.application.article

import kogayushi.tips.graphql.model.article.ArticleRepository
import org.springframework.stereotype.Service
import java.util.UUID

data class UnlikeArticleInputData(
    val articleId: UUID,
    val userId: UUID
)

@Service
class UnlikeArticle(
    private val articleRepository: ArticleRepository
) {
    fun handle(input: UnlikeArticleInputData) {

        val article = articleRepository.resolveByArticleId(input.articleId)
            ?: throw IllegalArgumentException("Article not found")
        val unliked = article.unlikedBy(input.userId)
        articleRepository.update(unliked)
    }
}
