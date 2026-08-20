package com.janapc.online_course_system.security.service

import com.janapc.online_course_system.security.config.JwtService
import com.janapc.online_course_system.security.dto.LoginRequest
import com.janapc.online_course_system.security.dto.RegisterRequest
import com.janapc.online_course_system.security.entity.Role
import com.janapc.online_course_system.security.entity.User
import com.janapc.online_course_system.security.exception.EmailAlreadyExistsException
import com.janapc.online_course_system.security.repository.UserRepository
import kotlin.test.assertEquals
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {
	@Mock
	private lateinit var authenticationManager: AuthenticationManager

	@Mock
	private lateinit var userRepository: UserRepository

	@Mock
	private lateinit var passwordEncoder: PasswordEncoder

	@Mock
	private lateinit var jwtService: JwtService

	@InjectMocks
	private lateinit var authService: AuthService

	@Nested
	@DisplayName("Login Tests")
	inner class LoginTests {
		@Test
		@DisplayName("Should login successfully and return JWT token")
		fun shouldLoginSuccessfully() {
			val request = LoginRequest("test@test.com", "password")
			val expectedToken = "mocked-jwt-token"
			whenever(jwtService.generateToken(request.email)).thenReturn(expectedToken)
			val response = authService.login(request)
			assertNotNull(response)
			assertEquals(expectedToken, response.token)
			assertEquals("Bearer", response.type)
			verify(authenticationManager).authenticate(
				any<UsernamePasswordAuthenticationToken>(),
			)
			verify(jwtService).generateToken(request.email)
		}

		@Test
		@DisplayName("Should throw BadCredentialsException when credentials are invalid")
		fun shouldThrowExceptionWhenCredentialsAreInvalid() {
			val request = LoginRequest("test@test.com", "wrongpassword")
			whenever(authenticationManager.authenticate(any())).thenThrow(BadCredentialsException::class.java)
			assertThrows<BadCredentialsException> { authService.login(request) }
			verify(jwtService, never()).generateToken(any())
		}
	}

	@Nested
	@DisplayName("Register Tests")
	inner class RegisterTests {
		@Test
		@DisplayName("Should register a new user successfully and return JWT token")
		fun shouldRegisterUserSuccessfully() {
			val request = RegisterRequest("new@test.com", "password123")
			val expectedToken = "mocked-jwt-token"
			val encodedPassword = "encoded_password_123"
			whenever(userRepository.findByEmail(request.email)).thenReturn(null)
			whenever(passwordEncoder.encode(request.password)).thenReturn(encodedPassword)
			whenever(jwtService.generateToken(request.email)).thenReturn(expectedToken)
			val response = authService.register(request)
			assertNotNull(response)
			assertEquals(expectedToken, response.token)
			argumentCaptor<User>().apply {
				verify(userRepository).save(capture())
				assertEquals(request.email, firstValue.email)
				assertEquals(encodedPassword, firstValue.password)
				assertEquals(Role.USER, firstValue.role)
			}
		}

		@Test
		@DisplayName("Should throw EmailAlreadyExistsException when email is already registered")
		fun shouldThrowExceptionWhenEmailAlreadyExists() {
			val request = RegisterRequest(email = "existing@test.com", password = "password123")
			val existingUser = User(id = 1L, email = request.email, password = "hashed_password")

			whenever(userRepository.findByEmail(request.email)).thenReturn(existingUser)

			assertThrows<EmailAlreadyExistsException> {
				authService.register(request)
			}

			verify(userRepository, never()).save(any())
			verify(jwtService, never()).generateToken(any())
		}
	}
}
