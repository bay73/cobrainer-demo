package cobrainer.demo.model

import cobrainer.demo.repository.WrappedUuid
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonValue
import org.apache.coyote.BadRequestException
import java.time.Instant
import java.util.UUID

data class JobArchitectureItemId(
    @get:JsonValue override val asUuid: UUID,
) : WrappedUuid<JobArchitectureItemId>{
    /** Used for JSON deserialization of the wrapper */
    @JsonCreator
    constructor(id: String) : this(UUID.fromString(id))

    override fun toString() = asUuid.toString()
}

enum class JobArchitectureLayer(
    @get:JsonValue val jsonValue: String,
) {
    ROOT("root"),
    FAMILY("family"),
    CLUSTER("cluster"),
    ROLE("role"),
    LEVEL("level"),
    ;

    companion object {
        const val LAYER_LIMIT = 8
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class JobArchitectureItem(
    val id: JobArchitectureItemId,
    val level: JobArchitectureLayer,
    val parentId: JobArchitectureItemId?,
    val parentLevel: JobArchitectureLayer?,
    val title: String,
    val description: String?,
    val childCount: Int,
    val creator: UserId?,
    val createdAt: Instant,
    var updatedAt: Instant,
) {
    companion object {
        const val TITLE_MIN_LENGTH = 3
        const val TITLE_LIMIT = 256
        const val DESCRIPTION_LIMIT = 100000
    }
}

data class JobArchitectureItemCreate(
    val level: JobArchitectureLayer,
    val title: String,
    val description: String?,
    var creator: UserId?,
)

class JobArchitectureWrongLayerException(
    layer: JobArchitectureLayer,
) : BadRequestException("Job architecture item $layer is not allowed here.")

class JobArchitectureItemNotFoundException(
    itemId: JobArchitectureItemId,
) : Exception("Job architecture item $itemId not found.")
