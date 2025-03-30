package kogayushi.tips.graphql.application.article

import kogayushi.tips.graphql.model.article.Article
import kogayushi.tips.graphql.model.article.ArticleRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EditArticle(
    private val articleRepository: ArticleRepository,
) {
    fun handle(inputData: EditArticleInputData): Article {
        val article = articleRepository.resolveByArticleId(inputData.articleId)
            ?: throw IllegalArgumentException("Article not found: ${inputData.articleId}")

        val updatedArticle = article.updated(
            title = inputData.title,
            content = inputData.content
        )

        articleRepository.update(updatedArticle)
        return updatedArticle
    }
}

data class EditArticleInputData(
    val articleId: UUID,
    val title: String,
    val content: String,
)
