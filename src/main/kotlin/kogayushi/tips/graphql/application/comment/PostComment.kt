package kogayushi.tips.graphql.application.comment

import kogayushi.tips.graphql.model.comment.Comment
import kogayushi.tips.graphql.model.comment.CommentRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PostComment(
    private val commentRepository: CommentRepository,
) {
    fun handle(inputData: PostCommentInputData): Comment {
        val comment = Comment(
            id = UUID.randomUUID(),
            articleId = inputData.articleId,
            content = inputData.content,
            authorId = inputData.authorId
        )
        return commentRepository.add(comment)
    }
}

data class PostCommentInputData(
    val articleId: UUID,
    val content: String,
    val authorId: UUID,
)
