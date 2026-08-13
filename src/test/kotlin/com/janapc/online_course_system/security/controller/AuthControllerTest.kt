package com.janapc.online_course_system.course.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.janapc.online_course_system.security.config.JwtFilter
import com.janapc.online_course_system.security.config.JwtService
import com.janapc.online_course_system.security.controller.AuthController
import com.janapc.online_course_system.security.dto.AuthResponse
import com.janapc.online_course_system.security.dto.LoginRequest
import com.janapc.online_course_system.security.dto.RegisterRequest
import com.janapc.online_course_system.security.service.AuthService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(
    controllers = [AuthController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [JwtFilter::class, JwtService::class],
        ),
    ],
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var authService: AuthService

    @Nested
    @DisplayName("POST /auth/login")
    inner class LoginEndpointTests {

        @Test
        @DisplayName("Should return 200 OK and JWT token when login is successful")
        fun shouldReturn200AndTokenOnSuccessfulLogin() {
            val request = LoginRequest("test@test.com", "password")
            val authResponse = AuthResponse(token = "mocked-jwt-token")
            whenever(authService.login(any())).thenReturn(authResponse)
            mockMvc.perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.token").value(authResponse.token))
                .andExpect(jsonPath("$.type").value("Bearer"))
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when credentials are invalid")
        fun shouldReturn401WhenCredentialsAreInvalid() {
            val request = LoginRequest("test@test.com", "wrongpassword")
            val expectedExceptionMessage = "Invalid email or password"
            whenever(authService.login(any())).thenThrow(
                BadCredentialsException("Bad credentials"),
            )
            mockMvc.perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.message").value(expectedExceptionMessage))
        }

        @Test
        @DisplayName("Should return 400 Bad Request when request body is invalid")
        fun shouldReturn400WhenLoginRequestIsInvalid() {
            val invalidRequest = LoginRequest(email = "invalid-email", password = "")
            mockMvc.perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.fields").isArray)
        }
    }

    @Nested
    @DisplayName("POST /auth/register")
    inner class RegisterEndpointTests {

        @Test
        @DisplayName("Should return 201 Created and JWT token when registration is successful")
        fun shouldReturn201AndTokenOnSuccessfulRegistration() {
            val request = RegisterRequest("new@test.com", "password123")
            val authResponse = AuthResponse(token = "mocked-jwt-token")
            whenever(authService.register(any())).thenReturn(authResponse)

            mockMvc.perform(
                post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.token").value(authResponse.token))
                .andExpect(jsonPath("$.type").value("Bearer"))
        }

        @Test
        @DisplayName("Should return 400 Bad Request when password is shorter than 6 characters")
        fun shouldReturn400WhenPasswordIsTooShort() {
            val invalidRequest = RegisterRequest(email = "test@test.com", password = "123")

            mockMvc.perform(
                post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
        }
    }
}
