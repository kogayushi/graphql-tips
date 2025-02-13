package kogayushi.tips.graphql.model.article

import java.util.UUID

data class Article(
    val id: UUID,
    val title: String,
    val content: String,
    val authorId: UUID,
    val likedBy: List<UUID>,
)
