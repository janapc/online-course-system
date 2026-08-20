package com.janapc.online_course_system.security.config

import com.janapc.online_course_system.security.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtFilter(
	private val jwtService: JwtService,
	private val userRepository: UserRepository,
) : OncePerRequestFilter() {
	override fun doFilterInternal(
		request: HttpServletRequest,
		response: HttpServletResponse,
		filterChain: FilterChain,
	) {
		val authHeader = request.getHeader("Authorization")
		if (authHeader?.startsWith("Bearer ") == true) {
			val token = authHeader.substring(7)
			val email = jwtService.extractUsername(token)
			if (email != null && SecurityContextHolder.getContext().authentication == null) {
				userRepository.findByEmail(email)?.let { user ->
					val auth = UsernamePasswordAuthenticationToken(user, null, user.authorities)
					SecurityContextHolder.getContext().authentication = auth
				}
			}
		}
		filterChain.doFilter(request, response)
	}
}
