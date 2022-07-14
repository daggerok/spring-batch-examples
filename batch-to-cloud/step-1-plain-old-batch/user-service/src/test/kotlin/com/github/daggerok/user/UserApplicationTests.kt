package com.github.daggerok.user

import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@TestInstance(PER_CLASS)
@SpringBootTest // (webEnvironment = RANDOM_PORT)
class UserApplicationTests(@Autowired val usersRepository: UsersRepository) {

    @Test
    fun `should test liquibase inserts`() {
        // given
        val users = usersRepository
            .findUsersByOrderByCreatedAtDesc()
            .onEach { logger.info { it } }

        // then
        assertThat(users).hasSize(2)
    }

    companion object : KLogging()
}
