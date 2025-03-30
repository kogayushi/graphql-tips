package kogayushi.tips.graphql.model.article

import kogayushi.tips.graphql.adapter.presentation.graphql.OmittableValue
import java.time.LocalDateTime
import java.util.UUID

data class Article(
    val id: UUID,
    val title: String,
    val content: String,
    val authorId: UUID,
    val likedBy: List<UUID>,
    val scheduledPublishDate: LocalDateTime?,
) {

    fun likedBy(userId: UUID): Article {
        return if (this.likedBy.contains(userId)) {
            this
        } else {
            this.copy(likedBy = this.likedBy + userId)
        }
    }

    fun unlikedBy(userId: UUID): Article {
        return if (this.likedBy.contains(userId)) {
            this.copy(likedBy = this.likedBy - userId)
        } else {
            this
        }
    }

    fun updated(title: OmittableValue<String>, content: OmittableValue<String>, scheduledPublishDate: OmittableValue<LocalDateTime?>): Article {
        val updatingTitle = if (title.isOmitted) {
            this.title
        } else {
            title.value!!
        }
        val updatingContent = if (content.isOmitted) {
            this.content
        } else {
            content.value!!
        }
        val updatingScheduledPublishDate = if (scheduledPublishDate.isOmitted) {
            this.scheduledPublishDate
        } else {
            scheduledPublishDate.value
        }
        return Article(
            id = this.id,
            title = updatingTitle,
            content = updatingContent,
            authorId = this.authorId,
            likedBy = this.likedBy,
            scheduledPublishDate = updatingScheduledPublishDate
        )
    }
}
