package kogayushi.tips.graphql.model.fundamental

import org.springframework.context.ApplicationEvent
import java.time.OffsetDateTime
import java.util.UUID

abstract class DomainEvent(source: Any) : ApplicationEvent(source) {
    abstract val id: UUID
    abstract val occurredAt: OffsetDateTime
}
