package com.janapc.online_course_system.security.dto


data class AuthResponse(
    val token: String,
    val type: String = "Bearer",
)
