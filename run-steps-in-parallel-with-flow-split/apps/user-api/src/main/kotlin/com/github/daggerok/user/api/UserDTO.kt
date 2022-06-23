package com.github.daggerok.user.api

import java.time.Instant

data class UserDTO(
    val id: Long = -1,
    val firstName: String = "",
    val lastName: String = "",
    val createdAt: Instant? = null,
)
