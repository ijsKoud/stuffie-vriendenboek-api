package nl.klrnbk.common

import kotlin.test.Test
import kotlin.test.assertEquals

class GreeterTest {
    @Test
    fun greetsByName() = assertEquals("Hello, world!", Greeter.greet("world"))
}
