package kogayushi.tips.graphql.model.comment

import kogayushi.tips.graphql.model.article.ArticleRepository
import kogayushi.tips.graphql.model.user.UserRepository
import org.springframework.stereotype.Component
import java.util.UUID

// 今回はデモなので、DIPは考えない
@Component
class CommentRepository {

    private val inMemory = mutableMapOf(
        ArticleRepository.ARTICLE_ID_1 to mutableListOf(
            Comment(
                id = COMMENT_ID_1,
                articleId = ArticleRepository.ARTICLE_ID_1,
                content = "この記事はとても参考になりました！",
                authorId = UserRepository.USER_ID_3
            ),
            Comment(
                id = COMMENT_ID_2,
                articleId = ArticleRepository.ARTICLE_ID_1,
                content = "Spring for GraphQLを使ってみたくなりました！",
                authorId = UserRepository.USER_ID_4
            )
        ),
        ArticleRepository.ARTICLE_ID_2 to mutableListOf(
            Comment(
                id = COMMENT_ID_3,
                articleId = ArticleRepository.ARTICLE_ID_2,
                content = "heapdumpの取得方法がわかりやすかったです！",
                authorId = UserRepository.USER_ID_5
            )
        )
    )

    fun resolveByArticleIds(
        articleIds: List<UUID>
    ): List<Comment> {
        return articleIds.flatMap { articleId ->
            inMemory[articleId] ?: emptyList()
        }
    }

    fun resolveByAuthorIds(authorIds: List<UUID>): List<Comment> {
        return inMemory.values.flatten().filter { it.authorId in authorIds }
    }

    fun add(comment: Comment): Comment {
        val comments = inMemory.getOrPut(comment.articleId) { mutableListOf() }
        comments.add(comment)
        return comment
    }

    companion object {
        val COMMENT_ID_1 = UUID.fromString("47ba3134-a46d-4124-a024-40da40d88bfe")
        val COMMENT_ID_2 = UUID.fromString("d089e9fb-abe0-40dc-a86a-80b3eb264131")
        val COMMENT_ID_3 = UUID.fromString("86287b95-abd3-42b4-835b-5edb7f82e847")
    }
}
