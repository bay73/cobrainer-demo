package cobrainer.demo

import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.jetbrains.exposed.spring.autoconfigure.ExposedAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication
@ImportAutoConfiguration(ExposedAutoConfiguration::class)
class DemoApplication

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}
