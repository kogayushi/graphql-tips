package kogayushi.tips.graphql.adapter.presentation.restfulapi.comment

import kogayushi.tips.graphql.model.comment.Comment
import java.util.UUID

data class CommentDto(
    val id: UUID,
    val articleId: UUID,
    val content: String,
    val authorId: UUID,
)

fun Comment.toCommentDto(): CommentDto =
    CommentDto(
        id = this.id,
        articleId = this.articleId,
        content = this.content,
        authorId = this.authorId,
    )