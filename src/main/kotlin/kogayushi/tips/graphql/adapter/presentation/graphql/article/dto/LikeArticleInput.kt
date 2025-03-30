package kogayushi.tips.graphql.adapter.presentation.graphql.article.dto

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class LikeArticleInput(
    @field:NotNull
    val articleId: UUID?
) {

    val articleIdAsNotNull by lazy { articleId!! }
}
