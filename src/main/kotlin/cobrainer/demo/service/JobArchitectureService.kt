package cobrainer.demo.service

import cobrainer.demo.model.JobArchitectureItem
import cobrainer.demo.model.JobArchitectureItemCreate
import cobrainer.demo.model.JobArchitectureItemId
import cobrainer.demo.model.JobArchitectureItemNotFoundException
import cobrainer.demo.model.JobArchitectureLayer
import cobrainer.demo.repository.JobArchitectureRepository
import org.springframework.stereotype.Component

@Component
class JobArchitectureService(
    private val jobArchitectureRepository: JobArchitectureRepository,
) {
    fun getMany(
        itemIds: List<JobArchitectureItemId>,
    ): List<JobArchitectureItem> = jobArchitectureRepository.getMany(itemIds)

    fun getOne(architectureItemId: JobArchitectureItemId): JobArchitectureItem = jobArchitectureRepository.getOne(architectureItemId) ?: throw JobArchitectureItemNotFoundException(
        architectureItemId
    )

    fun create(
        parentId: JobArchitectureItemId,
        architectureItem: JobArchitectureItemCreate,
    ): JobArchitectureItemId {
        val created = jobArchitectureRepository.create(parentId, architectureItem)
        modifyRootUpdatedAt(created)
        return created
    }

    fun update(
        architectureItemId: JobArchitectureItemId,
        title: String,
        description: String,
    ) {
        jobArchitectureRepository.update(architectureItemId, title, description)
        jobArchitectureRepository.markUpdated(architectureItemId)
        modifyRootUpdatedAt(architectureItemId)
    }

    fun deleteMany(
        itemIds: List<JobArchitectureItemId>,
        layer: JobArchitectureLayer,
    ) {
        val toDelete = jobArchitectureRepository.getMany(itemIds).filter { it.level == layer }
        jobArchitectureRepository.delete(toDelete.map { it.id })

        if (toDelete.isNotEmpty()) {
            toDelete.forEach {
                modifyRootUpdatedAt(it.id)
            }
        }
    }

    fun getRoot(architectureItemId: JobArchitectureItemId): JobArchitectureItem? {
        val item = getOne(architectureItemId)
        return if (item.level == JobArchitectureLayer.ROOT) item
        else getRoot(item.parentId!!)
    }

    fun modifyRootUpdatedAt(architectureItemId: JobArchitectureItemId) {
        getRoot(architectureItemId)?.let {
            jobArchitectureRepository.markUpdated(it.id)
        }
    }

}