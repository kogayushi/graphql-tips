package kogayushi.tips.graphql.adapter.presentation.graphql.article.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

data class EditArticleInput(
    @field:NotNull
    val articleId: UUID?,

    @field:NotBlank
    @field:Size(min = 3, max = 100)
    val title: String?,

    @field:NotBlank
    @field:Size(min = 10, max = 5000)
    val content: String?,
) {
    val articleIdAsNotNull by lazy { articleId!! }
    val titleAsNotNull by lazy { title!! }
    val contentAsNotNull by lazy { content!! }
}
