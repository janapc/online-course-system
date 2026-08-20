package com.janapc.online_course_system.security.config

import com.janapc.online_course_system.security.repository.UserRepository
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
	private val jwtFilter: JwtFilter,
) {
	@Bean
	fun userDetailsService(userRepository: UserRepository): UserDetailsService =
		UserDetailsService { email ->
			userRepository.findByEmail(email) ?: throw UsernameNotFoundException("User not found")
		}

	@Bean
	fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

	@Bean
	fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager = config.authenticationManager

	@Bean
	fun filterChain(http: HttpSecurity): SecurityFilterChain =
		http
			.csrf { it.disable() }
			.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
			.exceptionHandling {
				it.authenticationEntryPoint { _, response, _ ->
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
				}
			}.authorizeHttpRequests {
				it
					.requestMatchers(
						"/auth/**",
						"/v3/api-docs/**",
						"/swagger-ui/**",
						"/swagger-ui.html",
					).permitAll()
					.anyRequest()
					.authenticated()
			}.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
			.build()
}
