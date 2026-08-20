package com.janapc.online_course_system.security.controller

import com.janapc.online_course_system.security.dto.AuthResponse
import com.janapc.online_course_system.security.dto.LoginRequest
import com.janapc.online_course_system.security.dto.RegisterRequest
import com.janapc.online_course_system.security.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Authentication", description = "Endpoints for user authentication and account registration")
@RestController
@RequestMapping("/auth")
class AuthController(
	private val authService: AuthService,
) {
	@Operation(summary = "Authenticate user and return JWT token")
	@PostMapping("/login")
	fun login(
		@Valid @RequestBody request: LoginRequest,
	): AuthResponse = authService.login(request)

	@Operation(summary = "Register a new user account")
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	fun register(
		@Valid @RequestBody request: RegisterRequest,
	): AuthResponse = authService.register(request)
}
