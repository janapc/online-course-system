package com.janapc.online_course_system.enrollment

import com.fasterxml.jackson.databind.ObjectMapper
import com.janapc.online_course_system.course.dto.CourseResponse
import com.janapc.online_course_system.course.dto.CreateCourseRequest
import com.janapc.online_course_system.enrollment.dto.CreateEnrollmentRequest
import com.janapc.online_course_system.security.dto.AuthResponse
import com.janapc.online_course_system.security.dto.LoginRequest
import com.janapc.online_course_system.security.dto.RegisterRequest
import com.janapc.online_course_system.security.entity.Role
import com.janapc.online_course_system.student.dto.CreateStudentRequest
import com.janapc.online_course_system.student.dto.StudentResponse
import org.junit.jupiter.api.DisplayName
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
class EnrollmentE2ETest {
	@Autowired
	private lateinit var mockMvc: MockMvc

	@Autowired
	private lateinit var objectMapper: ObjectMapper

	private fun MockHttpServletRequestBuilder.bearer(token: String): MockHttpServletRequestBuilder =
		this.header(HttpHeaders.AUTHORIZATION, "Bearer $token")

	private fun MockHttpServletRequestBuilder.json(body: Any): MockHttpServletRequestBuilder =
		this.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body))

	private fun obtainAccessToken(
		email: String = "enrollment_e2e@test.com",
		password: String = "password123",
		role: Role = Role.ADMIN,
	): String {
		mockMvc.perform(post("/auth/register").json(RegisterRequest(email, password, role)))

		val loginResult =
			mockMvc
				.perform(
					post("/auth/login").json(LoginRequest(email, password)),
				).andExpect(status().isOk)
				.andReturn()

		val response =
			objectMapper.readValue(
				loginResult.response.contentAsString,
				AuthResponse::class.java,
			)
		return response.token
	}

	@Test
	@DisplayName(
		"Should execute full enrollment flow: Register/Login -> Create Student -> Create Course -> Enroll -> Verify Enrolled Courses -> Prevent Duplicate Enrollment",
	)
	fun shouldExecuteFullEnrollmentFlow() {
		val token = obtainAccessToken()

		// create a student
		val createStudentRequest = CreateStudentRequest("Student for Enrollment", "student_enrollment@test.com")
		val studentResult =
			mockMvc
				.perform(
					post("/students")
						.bearer(token)
						.json(createStudentRequest),
				).andExpect(status().isCreated)
				.andReturn()
		val studentId = objectMapper.readValue(studentResult.response.contentAsString, StudentResponse::class.java).id
		// create a course
		val createCourseRequest =
			CreateCourseRequest(
				name = "Kotlin Spring Boot Masterclass",
				description = "Complete guide to building production ready APIs",
			)
		val courseResult =
			mockMvc
				.perform(
					post("/courses")
						.bearer(token)
						.json(createCourseRequest),
				).andExpect(status().isCreated)
				.andReturn()
		val courseId = objectMapper.readValue(courseResult.response.contentAsString, CourseResponse::class.java).id
		// enroll student into course
		val enrollmentRequest = CreateEnrollmentRequest(studentId, courseId)
		mockMvc
			.perform(
				post("/enrollments")
					.bearer(token)
					.json(enrollmentRequest),
			).andExpect(status().isCreated)
			.andExpect(jsonPath("$.studentId").value(studentId))
			.andExpect(jsonPath("$.courseId").value(courseId))

		// query student's courses and verify presence
		mockMvc
			.perform(
				get("/students/$studentId/courses")
					.bearer(token),
			).andExpect(status().isOk)
			.andExpect(jsonPath("$[0].id").value(courseId))
			.andExpect(jsonPath("$[0].name").value("Kotlin Spring Boot Masterclass"))
		// attempt duplicate enrollment -> Should return 409 Conflict
		mockMvc
			.perform(
				post("/enrollments")
					.bearer(token)
					.json(enrollmentRequest),
			).andExpect(status().isConflict)
	}
}
