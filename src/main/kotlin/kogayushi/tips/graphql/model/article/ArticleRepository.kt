package kogayushi.tips.graphql.model.article

import kogayushi.tips.graphql.model.user.UserRepository
import org.springframework.stereotype.Component
import java.util.UUID

// 今回はデモなので、DIPは考えない
@Component
class ArticleRepository {

    private val inMemory = mutableListOf(
        Article(
            id = ARTICLE_ID_1,
            title = "小規模スタートアップにおけるSpring for GraphQL活用事例",
            content = """
                少人数スタートアップがなぜSpring for GraphQLを採用したかご説明します。
                また、採用してわかった課題とそれをどう解決したかについてもお話します。
            """.trimIndent(),
            authorId = UserRepository.USER_ID_1,
            likedBy = listOf(
                UserRepository.USER_ID_3,
                UserRepository.USER_ID_4
            )
        ),
        Article(
            id = ARTICLE_ID_2,
            title = "AWS ECS Fargateでのheapdump取得方法",
            content = """
                AWS ECS Fargateで動作するJavaアプリケーションのheapdumpを取得する方法について解説します。
                また、取得したheapdumpをローカルにダウンロードする方法についても説明します。
            """.trimIndent(),
            authorId = UserRepository.USER_ID_1,
            likedBy = listOf(
                UserRepository.USER_ID_5
            )
        ),
        Article(
            id = ARTICLE_ID_3,
            title = "Startupを応援するStartupに興味がある人はCrewwへ！",
            content = """
                CrewwはStartupを応援するStartupです。
                Startupが活躍するために必要なことを事業として提供しています。
                ぜひ、Crewwに興味がある人はCrewwへ！
            """.trimIndent(),
            authorId = UserRepository.USER_ID_2,
            likedBy = emptyList(),
        )
    )

    fun resolveAll(): List<Article> {
        return inMemory
    }

    fun resolveByAuthorIds(authorId: List<UUID>): List<Article> {
        return inMemory.filter { it.authorId in authorId }
    }

    fun resolveByArticleId(articleId: UUID): Article? {
        return inMemory.find { it.id == articleId }
    }

    fun resolveByArticleIds(articleIds: List<UUID>): List<Article> {
        return inMemory.filter { it.id in articleIds }
    }

    fun add(article: Article): Article {
        inMemory.add(article)
        return article
    }

    companion object {
        val ARTICLE_ID_1 = UUID.fromString("ca69942f-9ab9-481b-a6b8-8cd937020dbf")
        val ARTICLE_ID_2 = UUID.fromString("1bdc461b-d7cf-488b-88e6-5fb0f5f473a9")
        val ARTICLE_ID_3 = UUID.fromString("faaf9a53-e5f5-437c-860c-c668c2f57793")
    }
}
