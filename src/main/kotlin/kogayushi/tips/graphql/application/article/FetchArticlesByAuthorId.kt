package kogayushi.tips.graphql.application.article

import kogayushi.tips.graphql.model.article.Article
import kogayushi.tips.graphql.model.article.ArticleRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FetchArticlesByAuthorId(
    private val articleRepository: ArticleRepository,
) {

    fun handle(authorIds: List<UUID>): List<Article> {
        return articleRepository.resolveByAuthorIds(authorIds)
    }
}
