package kogayushi.tips.graphql.adapter.presentation.graphql.article

import kogayushi.tips.graphql.adapter.presentation.graphql.article.dto.Article
import kogayushi.tips.graphql.adapter.presentation.graphql.article.dto.EditArticleInput
import kogayushi.tips.graphql.adapter.presentation.graphql.article.dto.LikeArticleInput
import kogayushi.tips.graphql.adapter.presentation.graphql.article.dto.PostArticleInput
import kogayushi.tips.graphql.adapter.presentation.graphql.article.dto.UnlikeArticleInput
import kogayushi.tips.graphql.adapter.presentation.graphql.article.dto.toArticleDto
import kogayushi.tips.graphql.adapter.presentation.graphql.comment.dto.Comment
import kogayushi.tips.graphql.adapter.presentation.graphql.user.dto.User
import kogayushi.tips.graphql.application.article.EditArticle
import kogayushi.tips.graphql.application.article.EditArticleInputData
import kogayushi.tips.graphql.application.article.FetchArticles
import kogayushi.tips.graphql.application.article.FetchArticlesByArticleIds
import kogayushi.tips.graphql.application.article.FetchArticlesByAuthorId
import kogayushi.tips.graphql.application.article.LikeArticle
import kogayushi.tips.graphql.application.article.LikeArticleInputData
import kogayushi.tips.graphql.application.article.PostArticle
import kogayushi.tips.graphql.application.article.PostArticleInputData
import kogayushi.tips.graphql.application.article.UnlikeArticle
import kogayushi.tips.graphql.application.article.UnlikeArticleInputData
import kogayushi.tips.graphql.model.user.UserRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated

@Controller
class ArticleGraphQLController(
    private val fetchArticles: FetchArticles,
    private val fetchArticlesByAuthorId: FetchArticlesByAuthorId,
    private val fetchArticlesByArticleIds: FetchArticlesByArticleIds,
    private val postArticle: PostArticle,
    private val editArticle: EditArticle,
    private val likeArticle: LikeArticle,
    private val unlikeArticle: UnlikeArticle,
) {

    @QueryMapping
    fun articles(): List<Article> {
        Thread.sleep(100L)
        val articles = fetchArticles.handle()
        return articles.map { it.toArticleDto() }
    }

    @BatchMapping(field = "articles")
    fun articlesOfUser(users: List<User>): Map<User, List<Article>> {
        Thread.sleep(100L)
        val userIds = users.map { it.id }
        val articles = fetchArticlesByAuthorId.handle(userIds)
        return users.associateWith { user ->
            articles.filter { it.authorId == user.id }
                .map { it.toArticleDto() }
        }
    }

    @BatchMapping(field = "article")
    fun articlesOfComments(comments: List<Comment>): Map<Comment, Article> {
        Thread.sleep(100L)
        val articleIds = comments.map { it.articleId }
        val articles = fetchArticlesByArticleIds.handle(articleIds)
        return comments.associateWith { comment ->
            articles.find { it.id == comment.articleId }?.toArticleDto() ?: throw IllegalStateException(
                "detected coding bug: article not found for comment: $comment"
            )
        }
    }

    @MutationMapping
    fun postArticle(
        @Validated @Argument input: PostArticleInput
    ): Article {
        val inputData = PostArticleInputData(
            title = input.titleAsNotNull,
            content = input.contentAsNotNull,
            authorId = UserRepository.USER_ID_1
        )
        val article = postArticle.handle(inputData)
        return article.toArticleDto()
    }

    @MutationMapping
    fun editArticle(
        @Validated @Argument input: EditArticleInput
    ): Article {
        val inputData = EditArticleInputData(
            articleId = input.articleIdAsNotNull,
            title = if (input.isTitlePresent) input.titleValue else null,
            content = if (input.isContentPresent) input.contentValue else null
        )
        val article = editArticle.handle(inputData)
        return article.toArticleDto()
    }

    @MutationMapping
    fun likeArticle(
        @Validated @Argument input: LikeArticleInput
    ): Boolean {
        val inputData = LikeArticleInputData(
            articleId = input.articleIdAsNotNull,
            userId = UserRepository.USER_ID_3
        )
        likeArticle.handle(inputData)
        return true
    }

    @MutationMapping
    fun unlikeArticle(
        @Validated @Argument input: UnlikeArticleInput
    ): Boolean {
        val inputData = UnlikeArticleInputData(
            articleId = input.articleIdAsNotNull,
            userId = UserRepository.USER_ID_3
        )
        unlikeArticle.handle(inputData)

        return true
    }
}
