package nl.klrnbk.stuffie

import nl.klrnbk.spring.StuffieSpringApplicationAnnotation
import org.springframework.boot.runApplication

@StuffieSpringApplicationAnnotation class StuffieApplication

fun main(args: Array<String>) {
    runApplication<StuffieApplication>(*args)
}
