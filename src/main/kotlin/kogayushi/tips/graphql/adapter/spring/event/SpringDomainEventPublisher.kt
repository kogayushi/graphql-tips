package kogayushi.tips.graphql.adapter.spring.event

import kogayushi.tips.graphql.model.fundamental.DomainEvent
import kogayushi.tips.graphql.model.fundamental.DomainEventPublisher
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringDomainEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : DomainEventPublisher {
    override fun publish(event: DomainEvent) {
        applicationEventPublisher.publishEvent(event)
    }
}
