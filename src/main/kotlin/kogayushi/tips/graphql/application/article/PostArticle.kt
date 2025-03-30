package kogayushi.tips.graphql.application.article

import kogayushi.tips.graphql.model.article.Article
import kogayushi.tips.graphql.model.article.ArticleRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class PostArticle(
    private val articleRepository: ArticleRepository,
) {
    fun handle(inputData: PostArticleInputData): Article {
        val article = Article(
            id = UUID.randomUUID(),
            title = inputData.title,
            content = inputData.content,
            authorId = inputData.authorId,
            likedBy = emptyList(),
            scheduledPublishDate = inputData.scheduledPublishDate
        )
        return articleRepository.add(article)
    }
}

data class PostArticleInputData(
    val title: String,
    val content: String,
    val authorId: UUID,
    val scheduledPublishDate: LocalDateTime?,
)
