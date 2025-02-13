package kogayushi.tips.graphql.application.comment

import kogayushi.tips.graphql.model.comment.Comment
import kogayushi.tips.graphql.model.comment.CommentRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
data class FetchComments(
    private val commentRepository: CommentRepository,
) {

    fun handle(inputData: FetchCommentsInputData): List<Comment> {
        return commentRepository.resolveByArticleIds(inputData.articleIds)
    }
}


data class FetchCommentsInputData(
    val articleIds: List<UUID>
)