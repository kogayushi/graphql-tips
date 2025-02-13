package kogayushi.tips.graphql.model.user

import org.springframework.stereotype.Component
import java.util.UUID

// 今回はデモなので、DIPは考えない
@Component
class UserRepository {

    private val inMemory = mapOf(
        USER_ID_1 to User(
            id = USER_ID_1,
            name = "Alice"
        ),
        USER_ID_2 to User(
            id = USER_ID_2,
            name = "Bob"
        ),
        USER_ID_3 to User(
            id = USER_ID_3,
            name = "Charlie"
        ),
        USER_ID_4 to User(
            id = USER_ID_4,
            name = "Dave"
        ),
        USER_ID_5 to User(
            id = USER_ID_5,
            name = "Eve"
        )
    )

    fun resolveByIds(ids: List<UUID>): List<User> {
        return ids.mapNotNull { id ->
            inMemory[id]
        }
            .distinctBy { it.id }
    }

    fun findAll(): List<User> {
        return inMemory.values.toList()
    }

    companion object {
        val USER_ID_1 = UUID.fromString("890d121b-2824-4820-9661-308278a79284")
        val USER_ID_2 = UUID.fromString("dde6eb79-b7a5-4e86-b78d-39a99f333c50")
        val USER_ID_3 = UUID.fromString("006f4f30-08db-449e-83fb-08890bdefec5")
        val USER_ID_4 = UUID.fromString("dad30c4b-cff4-45b4-aadd-891be05181d5")
        val USER_ID_5 = UUID.fromString("829d4a0c-2971-40da-b995-a3dd614c238f")
    }
}
