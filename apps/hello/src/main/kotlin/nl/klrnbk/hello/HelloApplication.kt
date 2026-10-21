package nl.klrnbk.hello

import nl.klrnbk.common.Greeter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class HelloApplication

@RestController
class HelloController {
    @GetMapping("/hello")
    fun hello(@RequestParam(defaultValue = "world") name: String) = Greeter.greet(name)
}

fun main(args: Array<String>) {
    runApplication<HelloApplication>(*args)
}
