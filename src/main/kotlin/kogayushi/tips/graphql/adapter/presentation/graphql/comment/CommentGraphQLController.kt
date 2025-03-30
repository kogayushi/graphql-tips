package kogayushi.tips.graphql.adapter.presentation.graphql.comment

import kogayushi.tips.graphql.adapter.presentation.graphql.article.dto.Article
import kogayushi.tips.graphql.adapter.presentation.graphql.comment.dto.Comment
import kogayushi.tips.graphql.adapter.presentation.graphql.comment.dto.PostCommentInput
import kogayushi.tips.graphql.adapter.presentation.graphql.comment.dto.toCommentDto
import kogayushi.tips.graphql.adapter.presentation.graphql.user.dto.User
import kogayushi.tips.graphql.application.comment.FetchComments
import kogayushi.tips.graphql.application.comment.FetchCommentsInputData
import kogayushi.tips.graphql.application.comment.PostComment
import kogayushi.tips.graphql.application.comment.PostCommentInputData
import kogayushi.tips.graphql.model.user.UserRepository
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.validation.annotation.Validated
import java.util.UUID
import kogayushi.tips.graphql.application.user.FetchCommentsByAuthorIds

@Controller
class CommentGraphQLController(
    private val fetchComments: FetchComments,
    private val fetchCommentsByAuthorIds: FetchCommentsByAuthorIds,
    private val postComment: PostComment,
) {
    @QueryMapping
    fun comments(@Argument articleId: UUID): List<Comment> {
        val comments = fetchComments.handle(FetchCommentsInputData(listOf(articleId)))
        return comments.map { it.toCommentDto() }
    }

    @BatchMapping(field = "comments")
    fun commentsOfArticle(articles: List<Article>): Map<Article, List<Comment>> {
        val articleIds = articles.map { it.id }
        val comments = fetchComments.handle(FetchCommentsInputData(articleIds))
        Thread.sleep(100L)

        return articles.associateWith {
            val articleComments = comments.filter { comment -> comment.articleId == it.id }
            articleComments.map { it.toCommentDto() }
        }
    }

    @BatchMapping(field = "comments")
    fun commentsOfUser(users: List<User>): Map<User, List<Comment>> {
        val userIds = users.map { it.id }
        val comments = fetchCommentsByAuthorIds.handle(userIds)
        return users.associateWith { user ->
            comments.filter { it.authorId == user.id }
                .map { it.toCommentDto() }
        }
    }

    @MutationMapping
    fun postComment(
        @Validated @Argument input: PostCommentInput
    ): Comment {
        Thread.sleep(100L)
        val inputData = PostCommentInputData(
            articleId = input.articleIdAsNotNull,
            content = input.contentAsNotNull,
            authorId = UserRepository.USER_ID_2
        )
        val comment = postComment.handle(inputData)
        return comment.toCommentDto()
    }
}
