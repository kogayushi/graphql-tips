package kogayushi.tips.graphql.adapter.presentation.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/graphql-voyager")
@Controller
class GraphQLVoyagerController {
    @GetMapping
    fun voyager(): String {
        return "graphql-voyager"
    }
}
