package com.janapc.online_course_system.student

import com.fasterxml.jackson.databind.ObjectMapper
import com.janapc.online_course_system.security.dto.AuthResponse
import com.janapc.online_course_system.security.dto.LoginRequest
import com.janapc.online_course_system.security.dto.RegisterRequest
import com.janapc.online_course_system.security.entity.Role
import com.janapc.online_course_system.student.dto.CreateStudentRequest
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
class StudentE2ETest {
	@Autowired
	private lateinit var mockMvc: MockMvc

	@Autowired
	private lateinit var objectMapper: ObjectMapper

	private fun MockHttpServletRequestBuilder.bearer(token: String): MockHttpServletRequestBuilder =
		this.header(
			HttpHeaders.AUTHORIZATION,
			"Bearer $token",
		)

	private fun MockHttpServletRequestBuilder.json(body: Any): MockHttpServletRequestBuilder =
		this.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body))

	private fun obtainAccessToken(
		email: String = "test_e2e@test.com",
		password: String = "password123",
		role: Role = Role.ADMIN,
	): String {
		mockMvc.perform(post("/auth/register").json(RegisterRequest(email, password, role)))

		val result =
			mockMvc
				.perform(
					post("/auth/login").json(LoginRequest(email, password)),
				).andExpect(status().isOk)
				.andReturn()

		val response = objectMapper.readValue(result.response.contentAsString, AuthResponse::class.java)
		return response.token
	}

	@Nested
	@DisplayName("Authentication & Protected Routes")
	inner class ProtectedRoutesTests {
		@Test
		@DisplayName("Should return 401 Unauthorized when accessing /students without token")
		fun shouldReturn401WhenNoTokenProvided() {
			mockMvc
				.perform(get("/students"))
				.andExpect(status().isUnauthorized)
		}

		@Test
		@DisplayName("Should return 200 OK when accessing /students with valid token")
		fun shouldReturn200WhenValidTokenProvided() {
			val token = obtainAccessToken()

			mockMvc
				.perform(get("/students").bearer(token))
				.andExpect(status().isOk)
		}
	}

	@Nested
	@DisplayName("Student Operations")
	inner class StudentOperationsTests {
		@Test
		@DisplayName("Should create student successfully with valid token")
		fun shouldCreateStudentSuccessfully() {
			val token = obtainAccessToken()
			val request = CreateStudentRequest(name = "E2E Student", email = "e2e_student@test.com")

			mockMvc
				.perform(
					post("/students")
						.bearer(token)
						.json(request),
				).andExpect(status().isCreated)
				.andExpect(jsonPath("$.name").value("E2E Student"))
				.andExpect(jsonPath("$.email").value("e2e_student@test.com"))
		}

		@Test
		@DisplayName("Should filter students by name and status with pagination")
		fun shouldFilterStudentsWithPagination() {
			val token = obtainAccessToken()
			mockMvc
				.perform(
					get("/students")
						.bearer(token)
						.param("name", "E2E")
						.param("active", "true")
						.param("page", "0")
						.param("size", "5"),
				).andExpect(status().isOk)
				.andExpect(jsonPath("$.content").isArray)
				.andExpect(jsonPath("$.pageable.pageSize").value(5))
		}
	}

	@Nested
	@DisplayName("Validation")
	inner class ValidationTests {
		@Test
		@DisplayName("Should return 400 Bad Request when creating student with invalid data")
		fun shouldReturn400WhenDataIsInvalid() {
			val token = obtainAccessToken()
			val invalidRequest = CreateStudentRequest(name = "", email = "not-an-email")

			mockMvc
				.perform(
					post("/students")
						.bearer(token)
						.json(invalidRequest),
				).andExpect(status().isBadRequest)
				.andExpect(jsonPath("$.fields").isArray)
		}

		@Test
		@DisplayName("Should return 404 Not Found when searching for non-existing student")
		fun shouldReturn404ForMissingStudent() {
			val token = obtainAccessToken()

			mockMvc
				.perform(get("/students/999999").bearer(token))
				.andExpect(status().isNotFound)
		}
	}
}
