package kogayushi.tips.graphql.application.user

import kogayushi.tips.graphql.model.user.User
import kogayushi.tips.graphql.model.user.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FetchUsers(
    private val userRepository: UserRepository,
) {

    fun handle(inputData: FetchUsersInputData): List<User> {
        return userRepository.resolveByIds(inputData.userIds)
    }
}

data class FetchUsersInputData(
    val userIds: List<UUID>
)