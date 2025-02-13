package kogayushi.tips.graphql.adapter.presentation.restfulapi.user

import kogayushi.tips.graphql.application.user.FetchUsers
import kogayushi.tips.graphql.application.user.FetchUsersInputData
import kogayushi.tips.graphql.model.user.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserRestController(
    private val fetchUsers: FetchUsers

) {

    @GetMapping("/{userIds}")
    fun getUsers(
        @PathVariable userIds: List<UUID>
    ): List<User> {
        return fetchUsers.handle(
            FetchUsersInputData(
                userIds = userIds
            )
        )
    }
}