package dagggerok.users

import feign.Logger
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table
import mu.KLogging
import org.hibernate.annotations.CreationTimestamp
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@Configuration
class UsersConfig

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

data class UserDTO(
    val id: Long = -1,
    val firstName: String = "",
    val lastName: String = "",
    val createdAt: LocalDateTime? = null,
)

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
        logger.warn/*(e)*/ { "User service error: $error" }
        ResponseEntity.badRequest().body(mapOf("error" to error))
    }

    private companion object : KLogging()
}

@Configuration
@ConditionalOnMissingClass
@EnableFeignClients(clients = [UserClient::class])
class UserClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun feignLoggerLevel(): Logger.Level =
        Logger.Level.FULL;
}

@FeignClient(
    name = "user-client",
    url = "http://127.0.0.1:\${server.port}",
)
interface UserClient {

    @GetMapping("/api/users")
    fun getUsers(): List<UserDTO>

    @GetMapping("/api/users/{id}")
    fun getUser(@PathVariable("id") id: Long): UserDTO
}
