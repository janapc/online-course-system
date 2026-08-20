package com.janapc.online_course_system.enrollment.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.janapc.online_course_system.course.dto.CourseSummaryResponse
import com.janapc.online_course_system.enrollment.dto.CreateEnrollmentRequest
import com.janapc.online_course_system.enrollment.dto.EnrollmentDetailsResponse
import com.janapc.online_course_system.enrollment.dto.EnrollmentResponse
import com.janapc.online_course_system.enrollment.exception.EnrollmentAlreadyCancelledException
import com.janapc.online_course_system.enrollment.exception.EnrollmentNotFoundException
import com.janapc.online_course_system.enrollment.service.EnrollmentService
import com.janapc.online_course_system.security.config.JwtFilter
import com.janapc.online_course_system.security.config.JwtService
import com.janapc.online_course_system.student.dto.StudentSummaryResponse
import java.time.LocalDateTime
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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(
	controllers = [EnrollmentController::class],
	excludeAutoConfiguration = [SecurityAutoConfiguration::class],
	excludeFilters = [
		ComponentScan.Filter(
			type = FilterType.ASSIGNABLE_TYPE,
			classes = [JwtFilter::class, JwtService::class],
		),
	],
)
@AutoConfigureMockMvc(addFilters = false)
class EnrollmentControllerTest {
	@Autowired
	private lateinit var mockMvc: MockMvc

	@Autowired
	private lateinit var objectMapper: ObjectMapper

	@MockitoBean
	private lateinit var enrollmentService: EnrollmentService

	private val yesterday = LocalDateTime.now().minusDays(1)
	private val today = LocalDateTime.now()

	@Nested
	@DisplayName("POST /enrollments")
	inner class CreateTests {
		@Test
		@DisplayName("Should return 201 Created when enrollment request is valid")
		fun shouldCreateEnrollmentSuccessfully() {
			val request = CreateEnrollmentRequest(studentId = 1L, courseId = 1L)
			val response = createSimpleEnrollmentResponse()
			whenever(enrollmentService.create(request)).thenReturn(response)
			mockMvc
				.perform(
					post("/enrollments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)),
				).andExpect(status().isCreated)
				.andExpect(jsonPath("$.id").value(20L))
				.andExpect(jsonPath("$.studentId").value(1L))
				.andExpect(jsonPath("$.courseId").value(1L))
				.andExpect(jsonPath("$.active").isBoolean())
		}

		@Test
		@DisplayName("Should return 400 Bad Request when CreateEnrollmentRequest is invalid")
		fun shouldReturn400WhenCreateRequestIsInvalid() {
			val invalidRequest = CreateEnrollmentRequest(studentId = null, courseId = null)
			mockMvc
				.perform(
					post("/enrollments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidRequest)),
				).andExpect(status().isBadRequest)
				.andExpect(jsonPath("$.fields").isArray)
		}
	}

	@Nested
	@DisplayName("GET /enrollments")
	inner class FindAllTests {
		@Test
		@DisplayName("Should return 200 OK with paged enrollments")
		fun shouldReturnPagedEnrollments() {
			val page = PageImpl(listOf(createSimpleEnrollmentDetailsResponse()), PageRequest.of(0, 10), 1)
			whenever(enrollmentService.findAll(any())).thenReturn(page)

			mockMvc
				.perform(
					get("/enrollments")
						.contentType(MediaType.APPLICATION_JSON),
				).andExpect(status().isOk)
				.andExpect(jsonPath("$.content[0].id").value(20L))
				.andExpect(jsonPath("$.content[0].student").isNotEmpty())
				.andExpect(jsonPath("$.content[0].course").isNotEmpty())
				.andExpect(jsonPath("$.content[0].active").value(true))
		}
	}

	@Nested
	@DisplayName("GET /enrollments/{id}")
	inner class FindByIdTests {
		@Test
		@DisplayName("Should return 200 OK with enrollment when found")
		fun shouldReturnEnrollmentWhenFound() {
			whenever(enrollmentService.findById(20L)).thenReturn(createSimpleEnrollmentDetailsResponse())
			mockMvc
				.perform(get("/enrollments/20"))
				.andExpect(status().isOk)
				.andExpect(jsonPath("$.id").value(20))
				.andExpect(jsonPath("$.student").isNotEmpty())
				.andExpect(jsonPath("$.course").isNotEmpty())
				.andExpect(jsonPath("$.active").value(true))
		}

		@Test
		@DisplayName("Should return 404 Not Found when enrollment does not exist")
		fun shouldReturn404WhenEnrollmentNotFound() {
			whenever(enrollmentService.findById(99L)).thenThrow(EnrollmentNotFoundException(99))

			mockMvc
				.perform(get("/enrollments/99"))
				.andExpect(status().isNotFound)
				.andExpect(jsonPath("$.message").value("Enrollment not found with id 99"))
		}
	}

	@Nested
	@DisplayName("Patch /enrollments/{id}/cancel")
	inner class CancelTests {
		@Test
		@DisplayName("Should return 200 OK with enrollment is cancelled")
		fun shouldReturnEnrollmentWhenCancelled() {
			val enrollment = createSimpleEnrollmentResponse()
			val enrollmentCancelled = enrollment.copy(active = false)
			whenever(enrollmentService.cancel(20L)).thenReturn(enrollmentCancelled)
			mockMvc
				.perform(patch("/enrollments/20/cancel"))
				.andExpect(status().isOk)
				.andExpect(jsonPath("$.id").value(20))
				.andExpect(jsonPath("$.studentId").isNotEmpty())
				.andExpect(jsonPath("$.courseId").isNotEmpty())
				.andExpect(jsonPath("$.active").value(false))
		}

		@Test
		@DisplayName("Should return 409 Conflict when enrollment is cancelled")
		fun shouldReturn409WhenEnrollmentAlreadyCancelled() {
			val expectedMessage = "Enrollment with id 99 has been cancelled"
			whenever(enrollmentService.cancel(99L)).thenThrow(EnrollmentAlreadyCancelledException(99))
			mockMvc
				.perform(patch("/enrollments/99/cancel"))
				.andExpect(status().isConflict)
				.andExpect(jsonPath("$.message").value(expectedMessage))
		}
	}

	private fun createSimpleEnrollmentResponse(): EnrollmentResponse =
		EnrollmentResponse(
			id = 20L,
			studentId = 1L,
			courseId = 1L,
			enrolledAt = today,
			active = true,
		)

	private fun createSimpleEnrollmentDetailsResponse(): EnrollmentDetailsResponse =
		EnrollmentDetailsResponse(
			id = 20L,
			student =
				StudentSummaryResponse(
					id = 1L,
					name = "Test",
				),
			course =
				CourseSummaryResponse(
					id = 1L,
					name = "Kotlin",
				),
			active = true,
			enrolledAt = today,
			createdAt = yesterday,
			updatedAt = yesterday,
		)
}
