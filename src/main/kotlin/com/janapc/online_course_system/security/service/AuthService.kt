package com.janapc.online_course_system.security.service

import com.janapc.online_course_system.security.config.JwtService
import com.janapc.online_course_system.security.dto.AuthResponse
import com.janapc.online_course_system.security.dto.LoginRequest
import com.janapc.online_course_system.security.dto.RegisterRequest
import com.janapc.online_course_system.security.entity.User
import com.janapc.online_course_system.security.exception.EmailAlreadyExistsException
import com.janapc.online_course_system.security.repository.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
	private val authenticationManager: AuthenticationManager,
	private val userRepository: UserRepository,
	private val passwordEncoder: PasswordEncoder,
	private val jwtService: JwtService,
) {
	fun login(request: LoginRequest): AuthResponse {
		authenticationManager.authenticate(
			UsernamePasswordAuthenticationToken(request.email, request.password),
		)
		val token = jwtService.generateToken(request.email)
		return AuthResponse(token = token)
	}

	fun register(request: RegisterRequest): AuthResponse {
		if (userRepository.findByEmail(request.email) != null) {
			throw EmailAlreadyExistsException()
		}
		val user =
			User(
				email = request.email,
				password = passwordEncoder.encode(request.password),
				role = request.role,
			)
		userRepository.save(user)
		val token = jwtService.generateToken(request.email)
		return AuthResponse(token = token)
	}
}
