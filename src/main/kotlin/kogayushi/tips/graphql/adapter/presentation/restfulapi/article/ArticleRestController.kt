package kogayushi.tips.graphql.adapter.presentation.restfulapi.article

import kogayushi.tips.graphql.adapter.presentation.restfulapi.article.dto.ArticleDto
import kogayushi.tips.graphql.adapter.presentation.restfulapi.article.dto.toArticleDto
import kogayushi.tips.graphql.application.article.FetchArticles
import kogayushi.tips.graphql.application.article.FetchArticlesByAuthorId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/articles")
class ArticleRestController(
    private val fetchArticles: FetchArticles,
    private val fetchArticlesByAuthorId: FetchArticlesByAuthorId
) {

    @GetMapping
    fun getArticles(): List<ArticleDto> {
        return fetchArticles.handle().map { it.toArticleDto() }
    }

    @GetMapping("/by-authors/{authorIds}")
    fun getArticlesByAuthorIds(@PathVariable authorIds: List<UUID>): List<ArticleDto> {
        val articles = fetchArticlesByAuthorId.handle(authorIds)
        return articles.map { it.toArticleDto() }
    }
}
