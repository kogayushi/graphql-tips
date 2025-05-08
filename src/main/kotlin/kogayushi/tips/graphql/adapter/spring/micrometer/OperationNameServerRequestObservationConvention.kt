package kogayushi.tips.graphql.adapter.spring.micrometer

import org.springframework.boot.autoconfigure.graphql.GraphQlProperties
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention
import org.springframework.http.server.observation.ServerRequestObservationContext
import org.springframework.stereotype.Component

@Component
class OperationNameServerRequestObservationConvention(
    private val graphQlProperties: GraphQlProperties,
) : DefaultServerRequestObservationConvention() {
    override fun getContextualName(context: ServerRequestObservationContext): String {
        val contextualName = super.getContextualName(context)
        val operationName =
            if (context.pathPattern == graphQlProperties.path) {
                context.carrier.queryString
                    ?.split("&")
                    ?.find {
                        it.lowercase().startsWith("$QUERY_NAME_NO_DELIMITER=") ||
                            it.lowercase().startsWith("$QUERY_NAME_SNAKE=") ||
                            it.lowercase().startsWith("$QUERY_NAME_KEBAB=")
                    }?.let {
                        val value = it.split("=")[1]
                        "?$QUERY_NAME_KEBAB=$value"
                    } ?: ""
            } else {
                ""
            }
        return "$contextualName$operationName"
    }

    private companion object {
        private const val QUERY_NAME_NO_DELIMITER = "operationname"
        private const val QUERY_NAME_SNAKE = "operation_name"
        private const val QUERY_NAME_KEBAB = "operation-name"
    }
}
