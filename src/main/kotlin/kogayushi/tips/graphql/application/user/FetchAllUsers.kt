package kogayushi.tips.graphql.application.user

import kogayushi.tips.graphql.model.user.User
import kogayushi.tips.graphql.model.user.UserRepository
import org.springframework.stereotype.Service

@Service
class FetchAllUsers(
    private val userRepository: UserRepository,
) {

    fun handle(): List<User> {
        return userRepository.findAll()
    }
}
