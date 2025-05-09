package cobrainer.demo.model

import cobrainer.demo.repository.WrappedUuid
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import java.util.UUID

data class UserId(
    @get:JsonValue override val asUuid: UUID,
) : WrappedUuid<UserId> {
    /**
     * Used for JSON deserialization of the wrapper
     */
    @JsonCreator
    constructor(id: String) : this(UUID.fromString(id))

    override fun toString() = asUuid.toString()
}
