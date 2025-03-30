package kogayushi.tips.graphql.adapter.presentation.graphql.article.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.graphql.data.ArgumentValue
import java.time.LocalDateTime
import java.util.UUID

data class EditArticleInput(
    @field:NotNull
    val articleId: UUID?,

    val title: ArgumentValue<@NotBlank @Size(min = 3, max = 100) String>,

    val content: ArgumentValue<@NotBlank @Size(min = 10, max = 5000) String>,

    val scheduledPublishDate: ArgumentValue<LocalDateTime?>,
) {
    val articleIdAsNotNull by lazy { articleId!! }
}
