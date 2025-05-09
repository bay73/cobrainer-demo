package cobrainer.demo.repository

import cobrainer.demo.model.JobArchitectureItem
import cobrainer.demo.model.JobArchitectureItemCreate
import cobrainer.demo.model.JobArchitectureItemId
import cobrainer.demo.model.JobArchitectureLayer
import cobrainer.demo.model.JobArchitectureWrongLayerException
import cobrainer.demo.model.UserId
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class JobArchitectureRepository {
    fun getRoots(): List<JobArchitectureItem> =
        transaction {
            JobArchitectureItemTable
                .selectAll()
                .where { JobArchitectureItemTable.parent.isNull() }
                .toList()
                .let(JobArchitectureItemMapping::mapToModels)
        }

    fun getChildren(parentId: JobArchitectureItemId): List<JobArchitectureItem> =
        transaction {
            JobArchitectureItemTable
                .selectAll()
                .where { JobArchitectureItemTable.parent eq parentId }
                .orderBy(JobArchitectureItemTable.createdAt to SortOrder.ASC)
                .toList()
                .let(JobArchitectureItemMapping::mapToModels)
        }

    fun getOne(id: JobArchitectureItemId): JobArchitectureItem? =
        transaction {
            JobArchitectureItemTable
                .selectAll()
                .where { JobArchitectureItemTable.id eq id }
                .toList()
                .let(JobArchitectureItemMapping::mapToModels)
                .firstOrNull()
        }

    fun getMany(ids: List<JobArchitectureItemId>): List<JobArchitectureItem> =
         transaction {
            val results =
                JobArchitectureItemTable
                    .selectAll()
                    .where { JobArchitectureItemTable.id inList ids }
                    .toList()
                    .let(JobArchitectureItemMapping::mapToModels)
                    .associateBy { it.id }
             ids.mapNotNull { results[it] }
        }

    fun create(
        parentId: JobArchitectureItemId?,
        createItem: JobArchitectureItemCreate,
    ): JobArchitectureItemId =
        transaction {
            val parent = parentId?.let { getOne(it) }
            val parentLevel = parent?.level
            verifyParentLayer(createItem.level, parentLevel)
            val itemId = JobArchitectureItemId(UUID.randomUUID())

            JobArchitectureItemTable.insert {
                it[JobArchitectureItemTable.id] = itemId
                it[JobArchitectureItemTable.level] = createItem.level
                it[JobArchitectureItemTable.parent] = parentId
                it[JobArchitectureItemTable.parentLevel] = parentLevel
                it[JobArchitectureItemTable.title] = createItem.title
                it[JobArchitectureItemTable.description] = createItem.description
                it[JobArchitectureItemTable.creator] = createItem.creator
                it[JobArchitectureItemTable.updatedAt] = Instant.now()
            }

            itemId
        }

    fun update(
        id: JobArchitectureItemId,
        title: String,
        description: String,
    ) {
        transaction {
            JobArchitectureItemTable.update(where = {
                JobArchitectureItemTable.id eq id
            }) { update ->
                update[JobArchitectureItemTable.title] = title
                update[JobArchitectureItemTable.description] = description
            }
        }
    }

    fun markUpdated(
        id: JobArchitectureItemId,
    ) {
        transaction {
            JobArchitectureItemTable.update(where = {
                JobArchitectureItemTable.id eq id
            }) { update ->
                update[JobArchitectureItemTable.updatedAt] = Instant.now()
            }
        }
    }
    fun delete(ids: List<JobArchitectureItemId>): Boolean =
        transaction {
            JobArchitectureItemTable.deleteWhere { id inList ids } > 0
        }

    private fun verifyParentLayer(
        layer: JobArchitectureLayer,
        parentLayer: JobArchitectureLayer?,
    ) {
        val allowed = JobArchitectureLayersTable.selectAll().where { JobArchitectureLayersTable.id eq layer and (JobArchitectureLayersTable.parent eq parentLayer) }.count() > 0
        if (!allowed) {
            throw JobArchitectureWrongLayerException(layer)
        }
    }
}

object JobArchitectureLayersTable : Table("job_architecture_levels") {
    val id = enumerationByName("id", length = JobArchitectureLayer.LAYER_LIMIT, JobArchitectureLayer::class)
    val parent = enumerationByName("parent", length = JobArchitectureLayer.LAYER_LIMIT, JobArchitectureLayer::class).nullable()
}

object JobArchitectureItemTable : Table("job_architecture_items") {
    val id = uuid("id", JobArchitectureItemId::class)
    val level = enumerationByName("level", length = JobArchitectureLayer.LAYER_LIMIT, JobArchitectureLayer::class)
    val parent = uuid("parent", JobArchitectureItemId::class).nullable()
    val parentLevel = enumerationByName("parent_level", length = JobArchitectureLayer.LAYER_LIMIT, JobArchitectureLayer::class).nullable()
    val title = varchar("title", length = JobArchitectureItem.TITLE_LIMIT)
    val description = text("description").nullable()
    val creator = uuid("creator", UserId::class).nullable()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }
}

private object JobArchitectureItemMapping {
    fun mapToModels(rows: List<ResultRow>): List<JobArchitectureItem> {
        if (rows.isEmpty()) {
            return emptyList()
        }

        val childCounts = countChildren(rows)

        return rows.map { row ->
            val id = row[JobArchitectureItemTable.id]
            JobArchitectureItem(
                id = id,
                level = row[JobArchitectureItemTable.level],
                parentId = row[JobArchitectureItemTable.parent],
                parentLevel = row[JobArchitectureItemTable.parentLevel],
                title = row[JobArchitectureItemTable.title],
                description = row[JobArchitectureItemTable.description],
                childCount = childCounts[id] ?: 0,
                creator = row[JobArchitectureItemTable.creator],
                createdAt = row[JobArchitectureItemTable.createdAt],
                updatedAt = row[JobArchitectureItemTable.updatedAt],
            )
        }
    }

    private fun countChildren(rows: List<ResultRow>): Map<JobArchitectureItemId, Int> =
        with(JobArchitectureItemTable) {
            val itemCountAlias = id.count()

            select(parent, itemCountAlias)
                .where { parent inList rows.map { it[JobArchitectureItemTable.id] } }
                .groupBy(parent)
                .associate { it[parent]!! to it[itemCountAlias].toInt() }
        }
}
