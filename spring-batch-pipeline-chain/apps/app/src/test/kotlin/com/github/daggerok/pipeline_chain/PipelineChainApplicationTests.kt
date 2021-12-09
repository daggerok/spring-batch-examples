package com.github.daggerok.pipeline_chain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.boot.test.context.SpringBootTest

@TestInstance(PER_CLASS)
@SpringBootTest //(webEnvironment = RANDOM_PORT)
class PipelineChainApplicationTests {

    @Test
    fun `should test context`() {
    }
}
