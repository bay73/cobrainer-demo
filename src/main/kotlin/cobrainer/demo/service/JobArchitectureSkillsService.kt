package cobrainer.demo.service

import cobrainer.demo.model.JobArchitectureItem
import cobrainer.demo.model.JobArchitectureItemId
import cobrainer.demo.model.JobArchitectureLayer
import cobrainer.demo.model.Skill
import cobrainer.demo.model.SkillId
import cobrainer.demo.model.SkillWithLevel
import cobrainer.demo.model.UserId
import cobrainer.demo.repository.JobArchitectureSkillsRepository
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class JobArchitectureSkillsService(
    private val jobArchitectureSkillsRepository: JobArchitectureSkillsRepository,
    private val jobArchitectureService: JobArchitectureService,
    private val skillsService: SkillsService,
) {
    fun getItemsWithSkills(itemIds: List<JobArchitectureItemId>): List<JobArchitectureItemWithSkills> {
        return emptyList()
    }
}

data class JobArchitectureItemWithSkills(
    val item: JobArchitectureItem,
    val skills: List<SkillWithLevel>,
)