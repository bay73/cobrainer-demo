package cobrainer.demo.tests

import cobrainer.demo.model.JobArchitectureItemCreate
import cobrainer.demo.model.JobArchitectureLayer
import cobrainer.demo.model.JobArchitectureWrongLayerException
import cobrainer.demo.repository.JobArchitectureRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Testcontainers
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe

@SpringBootTest
@Testcontainers
@Transactional
class RepositoryTest {
    @Autowired
    private lateinit var jobArchitectureRepository: JobArchitectureRepository

    @Autowired
    private lateinit var jobArchitectureRepo: JobArchitectureRepository

    @AfterEach
    fun tearDown() {
        jobArchitectureRepo.delete(jobArchitectureRepo.getRoots().map { it.id })
    }

    @Test
    fun `Root element can be created without parent`() {
        val id = jobArchitectureRepo.create(null, randomJobArchitectureItemCreate(JobArchitectureLayer.ROOT, "New root"))

        val item = jobArchitectureRepo.getOne(id)
        item?.parentId shouldBe null
        item?.parentLevel shouldBe null
        item?.level shouldBe JobArchitectureLayer.ROOT
        item?.title shouldBe "New root"
    }

    @Test
    fun `Non root elements cannot be created without parent`() {
        JobArchitectureLayer.entries.filter { it != JobArchitectureLayer.ROOT }.forEach {
            assertThrows<JobArchitectureWrongLayerException> {
                jobArchitectureRepo.create(null, randomJobArchitectureItemCreate(level = it, "New element"))
            }
        }
    }

    @Test
    fun `Get roots returns root elements`() {
        val root1Id = jobArchitectureRepo.create(null, randomJobArchitectureItemCreate(JobArchitectureLayer.ROOT, "One root"))
        val root2Id = jobArchitectureRepo.create(null, randomJobArchitectureItemCreate(JobArchitectureLayer.ROOT, "Another root"))
        val familyId = jobArchitectureRepo.create(root1Id, randomJobArchitectureItemCreate(JobArchitectureLayer.FAMILY, "One family"))
        jobArchitectureRepo.create(familyId, randomJobArchitectureItemCreate(JobArchitectureLayer.CLUSTER, "One cluster"))

        val result = jobArchitectureRepo.getRoots()
        val expectedTitles = mapOf(root1Id to "One root", root2Id to "Another root")
        val expectedChildren = mapOf(root1Id to 1, root2Id to 0)

        result.forEach {
            it.id shouldBeIn listOf(root1Id, root2Id)
            it.level shouldBe JobArchitectureLayer.ROOT
            it.title shouldBe expectedTitles[it.id]
            it.childCount shouldBe expectedChildren[it.id]
            it.creator shouldBe null
        }
    }

    @Test
    fun `Get children returns only children for specific item`() {
        val rootId = jobArchitectureRepo.create(null, randomJobArchitectureItemCreate(JobArchitectureLayer.ROOT, "One root"))
        val familyId = jobArchitectureRepo.create(rootId, randomJobArchitectureItemCreate(JobArchitectureLayer.FAMILY, "One family"))
        val cluster1Id = jobArchitectureRepo.create(familyId, randomJobArchitectureItemCreate(JobArchitectureLayer.CLUSTER, "First cluster"))
        val cluster2Id = jobArchitectureRepo.create(familyId, randomJobArchitectureItemCreate(JobArchitectureLayer.CLUSTER, "Second cluster"))
        val cluster3Id = jobArchitectureRepo.create(familyId, randomJobArchitectureItemCreate(JobArchitectureLayer.CLUSTER, "Third cluster"))
        jobArchitectureRepo.create(cluster1Id, randomJobArchitectureItemCreate(JobArchitectureLayer.ROLE, "One role"))
        jobArchitectureRepo.create(cluster1Id, randomJobArchitectureItemCreate(JobArchitectureLayer.ROLE, "Another role"))

        val result = jobArchitectureRepo.getChildren(familyId)

        val expectedTitles = mapOf(cluster1Id to "First cluster", cluster2Id to "Second cluster", cluster3Id to "Third cluster")
        val expectedChildren = mapOf(cluster1Id to 2, cluster2Id to 0, cluster3Id to 0)

        result.forEach {
            it.id shouldBeIn listOf(cluster1Id, cluster2Id, cluster3Id)
            it.level shouldBe JobArchitectureLayer.CLUSTER
            it.parentId shouldBe familyId
            it.parentLevel shouldBe JobArchitectureLayer.FAMILY
            it.title shouldBe expectedTitles[it.id]
            it.childCount shouldBe expectedChildren[it.id]
            it.creator shouldBe null
        }
    }

    @Test
    fun `Whole hierarchy can be built`() {
        val rootId = jobArchitectureRepo.create(null, randomJobArchitectureItemCreate(JobArchitectureLayer.ROOT, "New root"))
        val familyId = jobArchitectureRepo.create(rootId, randomJobArchitectureItemCreate(JobArchitectureLayer.FAMILY, "New family"))
        val clusterId = jobArchitectureRepo.create(familyId, randomJobArchitectureItemCreate(JobArchitectureLayer.CLUSTER, "New cluster"))
        val roleId = jobArchitectureRepo.create(clusterId, randomJobArchitectureItemCreate(JobArchitectureLayer.ROLE, "New role"))
        val levelId = jobArchitectureRepo.create(roleId, randomJobArchitectureItemCreate(JobArchitectureLayer.LEVEL, "New level"))

        jobArchitectureRepo.getOne(rootId)?.level shouldBe JobArchitectureLayer.ROOT
        jobArchitectureRepo.getOne(familyId)?.parentLevel shouldBe JobArchitectureLayer.ROOT
        jobArchitectureRepo.getOne(clusterId)?.parentLevel shouldBe JobArchitectureLayer.FAMILY
        jobArchitectureRepo.getOne(roleId)?.parentLevel shouldBe JobArchitectureLayer.CLUSTER
        jobArchitectureRepo.getOne(levelId)?.parentLevel shouldBe JobArchitectureLayer.ROLE
    }

    @Test
    fun `Element title and parent can be updated`() {
        val rootId = jobArchitectureRepo.create(null, randomJobArchitectureItemCreate(JobArchitectureLayer.ROOT, "New root"))
        val familyId = jobArchitectureRepo.create(rootId, randomJobArchitectureItemCreate(JobArchitectureLayer.FAMILY, "One family"))
        val clusterId = jobArchitectureRepo.create(familyId, randomJobArchitectureItemCreate(JobArchitectureLayer.CLUSTER, "New cluster"))

        val oldItem = jobArchitectureRepo.getOne(clusterId)
        oldItem?.parentId shouldBe familyId
        oldItem?.parentLevel shouldBe JobArchitectureLayer.FAMILY
        oldItem?.level shouldBe JobArchitectureLayer.CLUSTER
        oldItem?.title shouldBe "New cluster"

        jobArchitectureRepo.update(
            clusterId,
            title = "New cluster title",
            description = "New cluster description",
        )

        val newItem = jobArchitectureRepo.getOne(clusterId)
        newItem?.parentId shouldBe familyId
        newItem?.parentLevel shouldBe JobArchitectureLayer.FAMILY
        newItem?.level shouldBe JobArchitectureLayer.CLUSTER
        newItem?.title shouldBe "New cluster title"
        newItem?.description shouldBe "New cluster description"
    }

    @Test
    fun `Elements can be deleted`() {
        val rootId = jobArchitectureRepo.create(null, randomJobArchitectureItemCreate(JobArchitectureLayer.ROOT, "New root"))
        val familyId = jobArchitectureRepo.create(rootId, randomJobArchitectureItemCreate(JobArchitectureLayer.FAMILY, "New family"))

        jobArchitectureRepo.delete(listOf(familyId))

        jobArchitectureRepo.getOne(familyId) shouldBe null
    }

    @Test
    fun `Element with children can be deleted with cascade`() {
        val rootId = jobArchitectureRepo.create(null, randomJobArchitectureItemCreate(JobArchitectureLayer.ROOT, "New root"))
        val familyId = jobArchitectureRepo.create(rootId, randomJobArchitectureItemCreate(JobArchitectureLayer.FAMILY, "New family"))
        val clusterId = jobArchitectureRepo.create(familyId, randomJobArchitectureItemCreate(JobArchitectureLayer.CLUSTER, "New cluster"))
        val roleId = jobArchitectureRepo.create(clusterId, randomJobArchitectureItemCreate(JobArchitectureLayer.ROLE, "New role"))

        jobArchitectureRepo.delete(listOf(familyId))

        jobArchitectureRepo.getOne(familyId) shouldBe null
        jobArchitectureRepo.getOne(clusterId) shouldBe null
        jobArchitectureRepo.getOne(roleId) shouldBe null
    }

    private fun randomJobArchitectureItemCreate(
        level: JobArchitectureLayer = JobArchitectureLayer.entries.random(),
        title: String = "randomString",
        description: String? = null,
    ) = JobArchitectureItemCreate(
        level = level,
        title = title,
        description = description,
        creator = null,
    )

}