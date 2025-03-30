package kogayushi.tips.graphql.adapter.presentation.graphql.article.dto

import java.time.LocalDateTime
import java.util.UUID
import kogayushi.tips.graphql.model.article.Article as DomainArticle

data class Article(
    val id: UUID,
    val title: String,
    val content: String,
    val authorId: UUID,
    val likedBy: List<UUID>,
    val scheduledPublishDate: LocalDateTime?,
)

fun DomainArticle.toArticleDto(): Article =
    Article(
        id = this.id,
        title = this.title,
        content = this.content,
        authorId = this.authorId,
        likedBy = this.likedBy,
        scheduledPublishDate = this.scheduledPublishDate
    )
