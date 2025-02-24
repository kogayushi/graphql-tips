package kogayushi.tips.graphql.adapter.presentation.graphql.user

import kogayushi.tips.graphql.adapter.presentation.graphql.article.dto.Article
import kogayushi.tips.graphql.adapter.presentation.graphql.comment.dto.Comment
import kogayushi.tips.graphql.adapter.presentation.graphql.user.dto.User
import kogayushi.tips.graphql.adapter.presentation.graphql.user.dto.toUserDto
import kogayushi.tips.graphql.application.user.FetchUsers
import kogayushi.tips.graphql.application.user.FetchUsersInputData
import kogayushi.tips.graphql.application.user.FetchAllUsers
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class UserGraphQLController(
    private val fetchUsers: FetchUsers,
    private val fetchAllUsers: FetchAllUsers,
) {

    @BatchMapping(field = "author")
    fun authorsOfArticle(articles: List<Article>): Map<Article, User> {
        val userIds = articles.map { it.authorId }
        val users = fetchUsers.handle(FetchUsersInputData(userIds))
        Thread.sleep(100L)
        return articles.associateWith {
            val user = users.find { user -> user.id == it.authorId } ?: throw IllegalStateException("User not found")
            user.toUserDto()
        }
    }

    @BatchMapping(field = "author")
    fun authorOfComment(comments: List<Comment>): Map<Comment, User> {
        val userIds = comments.map { it.authorId }
        val users = fetchUsers.handle(FetchUsersInputData(userIds))
        Thread.sleep(100L)
        return comments.associateWith {
            val user = users.find { user -> user.id == it.authorId } ?: throw IllegalStateException("User not found")
            user.toUserDto()
        }
    }

    @BatchMapping(field = "likedBy")
    fun likedByOfArticle(articles: List<Article>): Map<Article, List<User>> {
        val userIds = articles.map { it.likedBy }.flatten()
        val users = fetchUsers.handle(FetchUsersInputData(userIds))
        Thread.sleep(100L)
        return articles.associateWith {
            val likedBy = users.filter { user -> user.id in it.likedBy }
            likedBy.map { it.toUserDto() }
        }
    }

    @QueryMapping
    fun users(): List<User> {
        return fetchAllUsers.handle().map { it.toUserDto() }
    }
}
