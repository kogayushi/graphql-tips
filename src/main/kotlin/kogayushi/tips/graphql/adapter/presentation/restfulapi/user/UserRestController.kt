package kogayushi.tips.graphql.adapter.presentation.restfulapi.user

import kogayushi.tips.graphql.adapter.presentation.restfulapi.user.dto.UserDto
import kogayushi.tips.graphql.adapter.presentation.restfulapi.user.dto.toUserDto
import kogayushi.tips.graphql.application.user.FetchAllUsers
import kogayushi.tips.graphql.application.user.FetchUsers
import kogayushi.tips.graphql.application.user.FetchUsersInputData
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserRestController(
    private val fetchUsers: FetchUsers,
    private val fetchAllUser: FetchAllUsers

) {

    @GetMapping("/{userIds}")
    fun getUsers(
        @PathVariable userIds: List<UUID>
    ): List<UserDto> {
        Thread.sleep(100L)
        return fetchUsers.handle(
            FetchUsersInputData(
                userIds = userIds
            )
        ).map { it.toUserDto() }
    }

    @GetMapping
    fun getAllUsers(): List<UserDto> {
        Thread.sleep(100L)
        return fetchAllUser.handle()
            .map { it.toUserDto() }
    }
}
