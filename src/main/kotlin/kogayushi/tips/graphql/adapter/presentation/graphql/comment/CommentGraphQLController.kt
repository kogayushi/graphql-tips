package kogayushi.tips.graphql.adapter.presentation.graphql.comment

import kogayushi.tips.graphql.adapter.presentation.graphql.article.dto.Article
import kogayushi.tips.graphql.adapter.presentation.graphql.comment.dto.Comment
import kogayushi.tips.graphql.adapter.presentation.graphql.comment.dto.toCommentDto
import kogayushi.tips.graphql.adapter.presentation.graphql.user.dto.User
import kogayushi.tips.graphql.application.comment.FetchComments
import kogayushi.tips.graphql.application.comment.FetchCommentsInputData
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.stereotype.Controller
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.Argument
import java.util.UUID
import kogayushi.tips.graphql.application.user.FetchCommentsByAuthorIds

@Controller
class CommentGraphQLController(
    private val fetchComments: FetchComments,
    private val fetchCommentsByAuthorIds: FetchCommentsByAuthorIds,
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
}
