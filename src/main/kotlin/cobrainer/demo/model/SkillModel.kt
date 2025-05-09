package cobrainer.demo.model

import cobrainer.demo.repository.WrappedUuid
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonValue
import java.util.UUID

data class SkillId(
    @get:JsonValue override val asUuid: UUID,
) : WrappedUuid<SkillId> {
    /**
     * Used for JSON deserialization of the wrapper
     */
    @JsonCreator
    constructor(id: String) : this(UUID.fromString(id))

    override fun toString() = asUuid.toString()
}

data class Skill(
    val id: SkillId,
    val title: String,
    val description: String,
    val skillType: SkillType,
)

data class SkillWithLevel(
    val skill: Skill,
    val level: Short,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SkillFromDb(
    val id: SkillId,
    val level: Short,
)

enum class SkillType {
    HARD_SKILL,
    SOFT_SKILL,
    LANGUAGE_SKILL,
}