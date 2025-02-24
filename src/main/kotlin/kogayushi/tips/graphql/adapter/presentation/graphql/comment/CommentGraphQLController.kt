package kogayushi.tips.graphql.adapter.presentation.graphql.comment

import kogayushi.tips.graphql.adapter.presentation.graphql.article.dto.Article
import kogayushi.tips.graphql.adapter.presentation.graphql.comment.dto.Comment
import kogayushi.tips.graphql.adapter.presentation.graphql.comment.dto.toCommentDto
import kogayushi.tips.graphql.application.comment.FetchComments
import kogayushi.tips.graphql.application.comment.FetchCommentsInputData
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.stereotype.Controller

@Controller
class CommentGraphQLController(
    private val fetchComments: FetchComments,
) {

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
}