package kogayushi.tips.graphql.model.fundamental

interface DomainEventPublisher {
    fun publish(event: DomainEvent)
}
