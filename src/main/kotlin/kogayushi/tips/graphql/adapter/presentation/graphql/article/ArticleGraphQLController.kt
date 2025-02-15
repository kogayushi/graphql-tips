package kogayushi.tips.graphql.adapter.presentation.graphql.article

import kogayushi.tips.graphql.adapter.presentation.graphql.article.dto.Article
import kogayushi.tips.graphql.adapter.presentation.graphql.article.dto.toArticleDto
import kogayushi.tips.graphql.adapter.presentation.graphql.comment.dto.Comment
import kogayushi.tips.graphql.adapter.presentation.graphql.user.dto.User
import kogayushi.tips.graphql.application.article.FetchArticles
import kogayushi.tips.graphql.application.article.FetchArticlesByArticleIds
import kogayushi.tips.graphql.application.article.FetchArticlesByAuthorId
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ArticleGraphQLController(
    private val fetchArticles: FetchArticles,
    private val fetchArticlesByAuthorId: FetchArticlesByAuthorId,
    private val fetchArticlesByArticleIds: FetchArticlesByArticleIds,
) {

    @QueryMapping
    fun articles(): List<Article> {
        val articles = fetchArticles.handle()
        return articles.map { it.toArticleDto() }
    }

    @BatchMapping(field = "articles")
    fun articlesOfUser(users: List<User>): Map<User, List<Article>> {
        val userIds = users.map { it.id }
        val articles = fetchArticlesByAuthorId.handle(userIds)
        return users.associateWith { user ->
            articles.filter { it.authorId == user.id }
                .map { it.toArticleDto() }
        }
    }

    @BatchMapping(field = "article")
    fun articlesOfComments(comments: List<Comment>): Map<Comment, Article> {
        val articleIds = comments.map { it.articleId }
        val articles = fetchArticlesByArticleIds.handle(articleIds)
        return comments.associateWith { comment ->
            articles.find { it.id == comment.articleId }?.toArticleDto() ?: throw IllegalStateException(
                "detected coding bug: article not found for comment: $comment"
            )
        }
    }
}
