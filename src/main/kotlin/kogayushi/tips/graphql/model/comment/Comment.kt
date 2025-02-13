package kogayushi.tips.graphql.model.comment

import java.util.UUID

data class Comment(
    val id: UUID,
    val articleId: UUID,
    val content: String,
    val authorId: UUID,
)
