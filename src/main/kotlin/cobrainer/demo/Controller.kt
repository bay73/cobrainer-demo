package cobrainer.demo

import cobrainer.demo.service.JobArchitectureService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller(
    private val service: JobArchitectureService
) {

    @GetMapping("/job-artchitecture", consumes = ["application/json"])
    fun getArchitecture(
    ) {
    }
}
