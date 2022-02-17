package com.github.daggerok.user.client

import com.github.daggerok.user.api.UserDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    name = "user-client",
    url = "\${user-service.url}",
)
interface UserClient {

    @GetMapping("/api/users")
    fun getUsers(): List<UserDTO>

    @GetMapping("/api/users/{id}")
    fun getUser(@PathVariable("id") id: Long): UserDTO
}
