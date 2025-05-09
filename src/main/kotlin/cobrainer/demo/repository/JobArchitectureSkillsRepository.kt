package cobrainer.demo.repository

import cobrainer.demo.model.JobArchitectureItemId
import cobrainer.demo.model.JobArchitectureLayer
import cobrainer.demo.model.SkillFromDb
import cobrainer.demo.model.SkillId
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class JobArchitectureSkillsRepository {

    fun getAllByItemIds(itemIds: List<JobArchitectureItemId>): Map<JobArchitectureItemId, List<SkillFromDb>> {
        val read =
            transaction {
                JobArchitectureItemSkillsTable
                    .selectAll()
                    .where { JobArchitectureItemSkillsTable.itemId inList itemIds }
                    .groupBy { it[JobArchitectureItemSkillsTable.itemId] }
                    .mapValues { (_, rows) ->
                        rows
                            .map { it.toSkillFromDb() }
                    }
            }

        return itemIds.associateWith { read.getOrDefault(it, emptyList()) }
    }

    private fun ResultRow.toSkillFromDb(): SkillFromDb =
        SkillFromDb(
            this[JobArchitectureItemSkillsTable.skillId],
            this[JobArchitectureItemSkillsTable.skillLevel],
        )

}

object JobArchitectureItemSkillsTable : Table("job_architecture_item_skills") {
    val itemId = uuid("id", JobArchitectureItemId::class)
    val skillId = uuid("id", SkillId::class)
    val skillLevel = short("skill_level").default(0)
}
