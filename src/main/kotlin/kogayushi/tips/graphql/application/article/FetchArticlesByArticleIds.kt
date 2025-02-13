package kogayushi.tips.graphql.application.article

import kogayushi.tips.graphql.model.article.Article
import kogayushi.tips.graphql.model.article.ArticleRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FetchArticlesByArticleIds(
    private val articleRepository: ArticleRepository,
) {

    fun handle(articleIds: List<UUID>): List<Article> {
        return articleRepository.resolveByArticleIds(articleIds)
    }
}
