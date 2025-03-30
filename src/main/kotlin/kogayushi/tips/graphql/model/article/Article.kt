package kogayushi.tips.graphql.model.article

import java.util.UUID

data class Article(
    val id: UUID,
    val title: String,
    val content: String,
    val authorId: UUID,
    val likedBy: List<UUID>,
) {

    fun likedBy(userId: UUID): Article {
        return if(this.likedBy.contains(userId)) {
            this
        } else {
            this.copy(likedBy = this.likedBy + userId)
        }
    }

    fun unlikedBy(userId: UUID): Article {
        return if(this.likedBy.contains(userId)) {
            this.copy(likedBy = this.likedBy - userId)
        } else {
            this
        }
    }
}
