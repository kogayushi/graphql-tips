package kogayushi.tips.graphql.adapter.presentation.graphql.comment.dto

import java.util.UUID
import kogayushi.tips.graphql.model.comment.Comment as DomainComment

data class Comment(
    val id: UUID,
    val articleId: UUID,
    val content: String,
    val authorId: UUID,
)

fun DomainComment.toCommentDto(): Comment =
    Comment(
        id = this.id,
        articleId = this.articleId,
        content = this.content,
        authorId = this.authorId
    )