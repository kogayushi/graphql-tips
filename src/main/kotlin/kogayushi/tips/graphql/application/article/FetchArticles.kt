package kogayushi.tips.graphql.application.article

import kogayushi.tips.graphql.model.article.Article
import kogayushi.tips.graphql.model.article.ArticleRepository
import org.springframework.stereotype.Service

@Service
data class FetchArticles(
    private val articleRepository: ArticleRepository,
) {

    fun handle(): List<Article> {
        return articleRepository.resolveAll()
    }
}