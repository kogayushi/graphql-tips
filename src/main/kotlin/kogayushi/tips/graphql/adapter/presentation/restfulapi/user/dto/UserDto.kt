package kogayushi.tips.graphql.adapter.presentation.restfulapi.user.dto

import kogayushi.tips.graphql.model.user.User
import java.util.UUID

data class UserDto(
    val id: UUID,
    val name: String
)

fun User.toUserDto(): UserDto =
    UserDto(
        id = this.id,
        name = this.name
    )