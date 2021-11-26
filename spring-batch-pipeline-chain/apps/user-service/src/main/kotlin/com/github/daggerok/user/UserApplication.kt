package com.github.daggerok.user

import com.github.daggerok.user.api.UserDTO
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table
import mu.KLogging
import org.hibernate.annotations.CreationTimestamp
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class UserApplication

fun main(args: Array<String>) {
    runApplication<UserApplication>(*args)
}

@Entity
@Table(name = "users")
data class User(

    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long = -1,

    val firstName: String = "",
    val lastName: String = "",

    @CreationTimestamp
    val createdAt: LocalDateTime? = null,
)

interface UsersRepository : JpaRepository<User, Long> {
    fun findUsersByOrderByCreatedAtDesc(): List<User>
}

fun User.toDTO(): UserDTO =
    UserDTO(id, firstName, lastName, createdAt)

fun List<User>.toDTO(): List<UserDTO> =
    map { it.toDTO() }

@RestController
class UsersResource(private val usersRepository: UsersRepository) {

    @GetMapping("/api/users")
    fun getUsers(): List<UserDTO> =
        usersRepository.findUsersByOrderByCreatedAtDesc()
            .toDTO()

    @GetMapping("/api/users/{id}")
    fun getUser(@PathVariable("id") id: Long): UserDTO =
        usersRepository.findById(id)
            .orElseThrow { RuntimeException("User($id) not found") }
            .toDTO()

    @ExceptionHandler
    fun handleExceptions(e: Throwable) = let {
        val error = e.message ?: "Unknown"
        logger.warn(e) { "User service error: $error" }
        ResponseEntity.badRequest().body(mapOf("error" to error))
    }

    private companion object : KLogging()
}
