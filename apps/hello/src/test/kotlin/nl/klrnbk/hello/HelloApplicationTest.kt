package nl.klrnbk.hello

import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
class HelloApplicationTest {
    @Test
    fun contextLoads() {}

    @Test
    fun controllerGreets() = assertEquals("Hello, you!", HelloController().hello("you"))
}
