package kogayushi.tips.graphql.adapter.presentation.restfulapi.comment

import kogayushi.tips.graphql.application.comment.FetchComments
import kogayushi.tips.graphql.application.comment.FetchCommentsInputData
import kogayushi.tips.graphql.application.user.FetchCommentsByAuthorIds
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api")
class CommentRestController(
    private val fetchComments: FetchComments,
    private val fetchCommentsByAuthorIds: FetchCommentsByAuthorIds
) {

    @GetMapping("/articles/{articleIds}/comments")
    fun getComments(
        @PathVariable articleIds: List<UUID>
    ): List<CommentDto> {
        Thread.sleep(100L)
        return fetchComments.handle(
            FetchCommentsInputData(
                articleIds = articleIds
            )
        ).map {
            it.toCommentDto()
        }
    }

    @GetMapping("/comments/by-authors/{authorIds}")
    fun getCommentsByAuthorIds(@PathVariable authorIds: List<UUID>): List<CommentDto> {
        Thread.sleep(100L)
        val comments = fetchCommentsByAuthorIds.handle(authorIds)
        return comments.map { it.toCommentDto() }
    }
}
