package cobrainer.demo

import cobrainer.demo.model.JobArchitectureItemId
import cobrainer.demo.service.JobArchitectureService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class Controller(
    private val service: JobArchitectureService
) {
    @GetMapping("/job-architecture/{jobArchitectureId}")
    fun getArchitecture(
        @PathVariable jobArchitectureId: JobArchitectureItemId,
    ) {
    }
}
