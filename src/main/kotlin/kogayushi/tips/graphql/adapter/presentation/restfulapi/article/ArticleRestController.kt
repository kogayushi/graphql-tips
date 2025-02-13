package kogayushi.tips.graphql.adapter.presentation.restfulapi.article

import kogayushi.tips.graphql.adapter.presentation.restfulapi.article.dto.ArticleDto
import kogayushi.tips.graphql.adapter.presentation.restfulapi.article.dto.toArticleDto
import kogayushi.tips.graphql.application.article.FetchArticles
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/articles")
class ArticleRestController(
    private val fetchArticles: FetchArticles,
) {

    @GetMapping
    fun getArticles(): List<ArticleDto> {
        return fetchArticles.handle().map { it.toArticleDto() }
    }
}