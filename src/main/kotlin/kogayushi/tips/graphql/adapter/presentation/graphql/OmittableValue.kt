package kogayushi.tips.graphql.adapter.presentation.graphql

data class OmittableValue<T>(
    val isOmitted: Boolean,
    val value: T
)
