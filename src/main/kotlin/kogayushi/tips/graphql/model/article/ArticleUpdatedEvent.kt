package kogayushi.tips.graphql.model.article

import kogayushi.tips.graphql.model.fundamental.DomainEvent
import java.time.OffsetDateTime
import java.util.UUID

data class ArticleUpdatedEvent(
    override val id: UUID,
    val source: Article,
    override val occurredAt: OffsetDateTime,
): DomainEvent(source)
