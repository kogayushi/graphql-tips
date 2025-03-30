package kogayushi.tips.graphql.adapter.presentation.graphql.comment.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

data class PostCommentInput(
    @field:NotNull
    val articleId: UUID?,
    @field:NotBlank
    @field:Size(min = 3, max = 1000)
    val content: String?,
) {
    val articleIdAsNotNull by lazy { articleId!! }
    val contentAsNotNull by lazy { content!! }
}
