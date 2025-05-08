package kogayushi.tips.graphql.adapter.presentation.graphql.scalar

import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.Value
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import java.util.Locale

object VoidScalar {
    val INSTANCE: GraphQLScalarType =
        GraphQLScalarType.newScalar().name("Void")
            .description("represents NULL values")
            .coercing(VoidCoercing())
            .build()

    private class VoidCoercing : Coercing<Unit, Any?> {
        @Throws(CoercingSerializeException::class)
        override fun serialize(
            dataFetcherResult: Any,
            graphQLContext: GraphQLContext,
            locale: Locale,
        ): Any? {
            return null
        }

        @Throws(CoercingParseValueException::class)
        override fun parseValue(
            input: Any,
            context: GraphQLContext,
            locale: Locale,
        ) {
        }

        @Throws(CoercingParseLiteralException::class)
        override fun parseLiteral(
            input: Value<*>,
            variables: CoercedVariables,
            graphQLContext: GraphQLContext,
            locale: Locale,
        ) {
        }
    }
}
