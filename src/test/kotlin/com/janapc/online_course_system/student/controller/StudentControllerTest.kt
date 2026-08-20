package com.janapc.online_course_system.student.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.janapc.online_course_system.course.dto.CourseSummaryResponse
import com.janapc.online_course_system.enrollment.service.EnrollmentService
import com.janapc.online_course_system.security.config.JwtFilter
import com.janapc.online_course_system.security.config.JwtService
import com.janapc.online_course_system.student.dto.CreateStudentBatchRequest
import com.janapc.online_course_system.student.dto.CreateStudentRequest
import com.janapc.online_course_system.student.dto.StudentResponse
import com.janapc.online_course_system.student.dto.UpdateStudentRequest
import com.janapc.online_course_system.student.exception.StudentNotFoundException
import com.janapc.online_course_system.student.service.StudentService
import java.time.LocalDateTime
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
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
	controllers = [StudentController::class],
	excludeAutoConfiguration = [SecurityAutoConfiguration::class],
	excludeFilters = [
		ComponentScan.Filter(
			type = FilterType.ASSIGNABLE_TYPE,
			classes = [JwtFilter::class, JwtService::class],
		),
	],
)
@AutoConfigureMockMvc(addFilters = false)
class StudentControllerTest {
	@Autowired
	private lateinit var mockMvc: MockMvc

	@Autowired
	private lateinit var objectMapper: ObjectMapper

	@MockitoBean
	private lateinit var studentService: StudentService

	@MockitoBean
	private lateinit var enrollmentService: EnrollmentService

	private val now = LocalDateTime.now()

	private val sampleStudentResponse =
		StudentResponse(
			id = 1L,
			name = "Test Silva",
			email = "test@test.com",
			active = true,
			createdAt = now,
			updatedAt = now,
		)

	@Nested
	@DisplayName("GET /students")
	inner class FindAllTests {
		@Test
		@DisplayName("Should return 200 OK with paged students")
		fun shouldReturnPagedStudents() {
			val page = PageImpl(listOf(sampleStudentResponse), PageRequest.of(0, 10), 1)
			whenever(studentService.findAll(any(), any(), any())).thenReturn(page)

			mockMvc
				.perform(
					get("/students")
						.param("name", "Test")
						.param("active", "true")
						.contentType(MediaType.APPLICATION_JSON),
				).andExpect(status().isOk)
				.andExpect(jsonPath("$.content[0].id").value(1))
				.andExpect(jsonPath("$.content[0].name").value("Test Silva"))
				.andExpect(jsonPath("$.content[0].email").value("test@test.com"))
				.andExpect(jsonPath("$.totalElements").value(1))
		}
	}

	@Nested
	@DisplayName("POST /students")
	inner class CreateTests {
		@Test
		@DisplayName("Should return 201 Created when student request is valid")
		fun shouldCreateStudentSuccessfully() {
			val request = CreateStudentRequest(name = "Test Silva", email = "test@test.com")
			whenever(studentService.create(any())).thenReturn(sampleStudentResponse)

			mockMvc
				.perform(
					post("/students")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)),
				).andExpect(status().isCreated)
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Test Silva"))
		}

		@Test
		@DisplayName("Should return 400 Bad Request when CreateStudentRequest is invalid")
		fun shouldReturn400WhenCreateRequestIsInvalid() {
			val invalidRequest = CreateStudentRequest(name = "", email = "invalid-email")

			mockMvc
				.perform(
					post("/students")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidRequest)),
				).andExpect(status().isBadRequest)
				.andExpect(jsonPath("$.fields").isArray)
		}
	}

	@Nested
	@DisplayName("GET /students/{id}")
	inner class FindByIdTests {
		@Test
		@DisplayName("Should return 200 OK with student when found")
		fun shouldReturnStudentWhenFound() {
			whenever(studentService.findById(1L)).thenReturn(sampleStudentResponse)

			mockMvc
				.perform(get("/students/1"))
				.andExpect(status().isOk)
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Test Silva"))
				.andExpect(jsonPath("$.email").value("test@test.com"))
		}

		@Test
		@DisplayName("Should return 404 Not Found when student does not exist")
		fun shouldReturn404WhenStudentNotFound() {
			whenever(studentService.findById(99L)).thenThrow(StudentNotFoundException(99))

			mockMvc
				.perform(get("/students/99"))
				.andExpect(status().isNotFound)
				.andExpect(jsonPath("$.message").value("Student with id 99 not found"))
		}
	}

	@Nested
	@DisplayName("PUT /students/{id}")
	inner class UpdateTests {
		@Test
		@DisplayName("Should return 200 OK when update is successful")
		fun shouldUpdateStudentSuccessfully() {
			val request = UpdateStudentRequest(name = "Test Updated", active = false, email = "test@test.com")
			val updatedResponse = sampleStudentResponse.copy(name = "Test Updated", active = false)

			whenever(studentService.update(1L, request)).thenReturn(updatedResponse)

			mockMvc
				.perform(
					put("/students/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)),
				).andExpect(status().isOk)
				.andExpect(jsonPath("$.name").value("Test Updated"))
				.andExpect(jsonPath("$.active").value(false))
		}

		@Test
		@DisplayName("Should return 404 Not Found when updating non-existing student")
		fun shouldReturn404WhenUpdatingNonExistingStudent() {
			val request = UpdateStudentRequest(name = "Test Updated", active = false, email = "test@test.com")
			whenever(studentService.update(99L, request)).thenThrow(StudentNotFoundException(99))

			mockMvc
				.perform(
					put("/students/99")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)),
				).andExpect(status().isNotFound)
				.andExpect(jsonPath("$.message").value("Student with id 99 not found"))
		}
	}

	@Nested
	@DisplayName("DELETE /students/{id}")
	inner class DeleteTests {
		@Test
		@DisplayName("Should return 204 No Content when deletion is successful")
		fun shouldDeleteStudentSuccessfully() {
			doNothing().whenever(studentService).delete(1L)

			mockMvc
				.perform(delete("/students/1"))
				.andExpect(status().isNoContent)
		}

		@Test
		@DisplayName("Should return 404 Not Found when deleting non-existing student")
		fun shouldReturn404WhenDeletingNonExistingStudent() {
			doThrow(StudentNotFoundException(99)).whenever(studentService).delete(99L)

			mockMvc
				.perform(delete("/students/99"))
				.andExpect(status().isNotFound)
		}
	}

	@Nested
	@DisplayName("GET /students/{id}/courses")
	inner class FindCoursesTests {
		@Test
		@DisplayName("Should return 200 OK with list of courses for the student")
		fun shouldReturnStudentCourses() {
			val courseSummary =
				CourseSummaryResponse(
					id = 10L,
					name = "Kotlin for Beginners",
				)
			whenever(enrollmentService.findCoursesByStudent(1L)).thenReturn(listOf(courseSummary))

			mockMvc
				.perform(get("/students/1/courses"))
				.andExpect(status().isOk)
				.andExpect(jsonPath("$[0].id").value(10))
				.andExpect(jsonPath("$[0].name").value("Kotlin for Beginners"))
		}
	}

	@Nested
	@DisplayName("POST /students/batch")
	inner class CreateBatchTests {
		@Test
		@DisplayName("Should return 202 Accepted when batch request is valid")
		fun shouldCreateBatchSuccessfully() {
			val student1 = CreateStudentRequest(name = "Test One", email = "one@test.com")
			val student2 = CreateStudentRequest(name = "Test Two", email = "two@test.com")
			val request = CreateStudentBatchRequest(students = listOf(student1, student2))

			doNothing().whenever(studentService).sendStudentsToQueue(request)

			mockMvc
				.perform(
					post("/students/batch")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)),
				).andExpect(status().isAccepted)

			verify(studentService).sendStudentsToQueue(request)
		}

		@Test
		@DisplayName("Should return 400 Bad Request when batch request is invalid")
		fun shouldReturn400WhenBatchRequestIsInvalid() {
			val invalidRequest = CreateStudentBatchRequest(students = emptyList())

			mockMvc
				.perform(
					post("/students/batch")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidRequest)),
				).andExpect(status().isBadRequest)
				.andExpect(jsonPath("$.fields").isArray)
		}
	}
}
