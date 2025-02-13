package kogayushi.tips.graphql.application.user

import kogayushi.tips.graphql.model.comment.Comment
import kogayushi.tips.graphql.model.comment.CommentRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FetchCommentsByAuthorIds(
    private val commentRepository: CommentRepository,
) {

    fun handle(authorIds: List<UUID>): List<Comment> {
        return commentRepository.resolveByAuthorIds(authorIds)
    }
}
