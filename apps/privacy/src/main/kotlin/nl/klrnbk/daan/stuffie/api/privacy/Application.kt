package nl.klrnbk.daan.stuffie.api.privacy

import nl.klrnbk.daan.stuffie.api.privacy.config.ApplicationProperties
import nl.klrnbk.spring.StuffieSpringApplicationAnnotation
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(ApplicationProperties::class)
@StuffieSpringApplicationAnnotation
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
