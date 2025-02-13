package kogayushi.tips.graphql.adapter.presentation.restfulapi.article.dto

import kogayushi.tips.graphql.model.article.Article
import java.util.UUID

data class ArticleDto(
    val id: UUID,
    val title: String,
    val content: String,
    val authorId: UUID,
    val likedBy: List<UUID>,
)

fun Article.toArticleDto(): ArticleDto =
    ArticleDto(
        id = this.id,
        title = this.title,
        content = this.content,
        authorId = this.authorId,
        likedBy = this.likedBy,
    )