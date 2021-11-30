package com.github.daggerok.retry

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@TestInstance(PER_CLASS)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ApplicationTests {

    @Test
    fun `should test context`() {
    }
}
