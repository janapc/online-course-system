package com.janapc.online_course_system.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.janapc.online_course_system.security.dto.LoginRequest
import com.janapc.online_course_system.security.dto.RegisterRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthE2ETest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private fun MockHttpServletRequestBuilder.json(body: Any): MockHttpServletRequestBuilder =
        this.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body))

    private fun MockHttpServletRequestBuilder.bearer(token: String): MockHttpServletRequestBuilder =
        this.header(HttpHeaders.AUTHORIZATION, "Bearer $token")

    @Nested
    @DisplayName("POST /auth/register")
    inner class RegisterTests {

        @Test
        @DisplayName("Should register a new user successfully")
        fun shouldRegisterUserSuccessfully() {
            val request = RegisterRequest(email = "new_auth_e2e@test.com", password = "password123")

            mockMvc.perform(
                post("/auth/register").json(request),
            )
                .andExpect(status().isCreated)
        }

        @Test
        @DisplayName("Should return 409 Conflict when registering duplicate email")
        fun shouldReturn409WhenEmailAlreadyExists() {
            val request = RegisterRequest(email = "duplicate_auth_e2e@test.com", password = "password123")

            mockMvc.perform(post("/auth/register").json(request))
                .andExpect(status().isCreated)

            mockMvc.perform(post("/auth/register").json(request))
                .andExpect(status().isConflict)
        }

        @Test
        @DisplayName("Should return 400 Bad Request when request body is invalid")
        fun shouldReturn400WhenRegisterRequestIsInvalid() {
            val invalidRequest = RegisterRequest(email = "invalid-email", password = "123")

            mockMvc.perform(post("/auth/register").json(invalidRequest))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.fields").isArray)
        }
    }

    @Nested
    @DisplayName("POST /auth/login")
    inner class LoginTests {

        @Test
        @DisplayName("Should login successfully and return JWT token")
        fun shouldLoginSuccessfully() {
            val registerRequest = RegisterRequest(email = "login_success@test.com", password = "password123")
            mockMvc.perform(post("/auth/register").json(registerRequest))
                .andExpect(status().isCreated)

            val loginRequest = LoginRequest(email = "login_success@test.com", password = "password123")

            mockMvc.perform(post("/auth/login").json(loginRequest))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when password is incorrect")
        fun shouldReturn401WhenPasswordIsIncorrect() {
            val registerRequest = RegisterRequest(email = "login_fail@test.com", password = "password123")
            mockMvc.perform(post("/auth/register").json(registerRequest))

            val invalidLoginRequest = LoginRequest(email = "login_fail@test.com", password = "wrongpassword")

            mockMvc.perform(post("/auth/login").json(invalidLoginRequest))
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
        }
    }

    @Nested
    @DisplayName("Token Verification")
    inner class TokenSecurityTests {
        @Test
        @DisplayName("Should return 401 Unauthorized when token is forged or invalid")
        fun shouldReturn401WhenTokenIsInvalid() {
            val forgedToken = "invalid.jwt.token.here"

            mockMvc.perform(
                get("/students").bearer(forgedToken),
            )
                .andExpect(status().isUnauthorized)
        }
    }
}
