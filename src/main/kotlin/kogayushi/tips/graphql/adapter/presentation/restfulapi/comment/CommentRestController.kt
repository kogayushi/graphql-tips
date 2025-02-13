package kogayushi.tips.graphql.adapter.presentation.restfulapi.comment

import kogayushi.tips.graphql.application.comment.FetchComments
import kogayushi.tips.graphql.application.comment.FetchCommentsInputData
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/articles")
class CommentRestController(private val fetchComments: FetchComments) {

    @GetMapping("/{articleIds}/comments")
    fun getComments(
        @PathVariable articleIds: List<UUID>
    ): List<CommentDto> {
        return fetchComments.handle(
            FetchCommentsInputData(
                articleIds = articleIds
            )
        ).map {
            it.toCommentDto()
        }
    }
}