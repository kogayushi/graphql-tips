package kogayushi.tips.graphql.adapter.presentation.graphql.user.dto

import java.util.UUID
import kogayushi.tips.graphql.model.user.User as DomainUser

data class User(
    val id: UUID,
    val name: String
)

fun DomainUser.toUserDto(): User =
    User(
        id = this.id,
        name = this.name
    )