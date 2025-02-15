package kogayushi.tips.graphql.adapter.presentation.graphql.article

import kogayushi.tips.graphql.adapter.presentation.graphql.article.dto.Article
import kogayushi.tips.graphql.adapter.presentation.graphql.article.dto.toArticleDto
import kogayushi.tips.graphql.application.article.FetchArticles
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ArticleGraphQLController(
    private val fetchArticles: FetchArticles,
) {

    @QueryMapping
    fun articles(): List<Article> {
        val articles = fetchArticles.handle()
        return articles.map { it.toArticleDto() }
    }
}