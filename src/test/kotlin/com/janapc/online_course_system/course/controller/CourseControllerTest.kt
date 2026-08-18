package com.janapc.online_course_system.course.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.janapc.online_course_system.course.dto.CourseResponse
import com.janapc.online_course_system.course.dto.CreateCourseBatchRequest
import com.janapc.online_course_system.course.dto.CreateCourseRequest
import com.janapc.online_course_system.course.dto.UpdateCourseRequest
import com.janapc.online_course_system.course.exception.CourseNotFoundException
import com.janapc.online_course_system.course.service.CourseService
import com.janapc.online_course_system.enrollment.service.EnrollmentService
import com.janapc.online_course_system.security.config.JwtFilter
import com.janapc.online_course_system.security.config.JwtService
import com.janapc.online_course_system.student.dto.StudentSummaryResponse
import java.time.LocalDateTime
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.verify
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
    controllers = [CourseController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [JwtFilter::class, JwtService::class],
        ),
    ],
)
@AutoConfigureMockMvc(addFilters = false)
class CourseControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var courseService: CourseService

    @MockitoBean
    private lateinit var enrollmentService: EnrollmentService

    private val now = LocalDateTime.now()

    private val sampleCourseResponse = CourseResponse(
        id = 1L,
        name = "Kotlin Basic",
        description = "Kotlin Basic description",
        active = true,
        createdAt = now,
        updatedAt = now,
    )

    @Nested
    @DisplayName("POST /courses")
    inner class CreateTests {

        @Test
        @DisplayName("Should return 201 Created when course request is valid")
        fun shouldCreateCourseSuccessfully() {
            val request = CreateCourseRequest(name = "Kotlin Basic", description = "Kotlin Basic description")
            whenever(courseService.create(request)).thenReturn(sampleCourseResponse)
            mockMvc.perform(
                post("/courses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Kotlin Basic"))
                .andExpect(jsonPath("$.description").value("Kotlin Basic description"))
                .andExpect(jsonPath("$.active").isBoolean())
        }

        @Test
        @DisplayName("Should return 400 Bad Request when CreateCourseRequest is invalid")
        fun shouldReturn400WhenCreateRequestIsInvalid() {
            val invalidRequest = CreateCourseRequest(name = "", description = "Kotlin Basic description")
            mockMvc.perform(
                post("/courses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.fields").isArray)
        }
    }

    @Nested
    @DisplayName("GET /courses")
    inner class FindAllTests {

        @Test
        @DisplayName("Should return 200 OK with paged courses")
        fun shouldReturnPagedCourses() {
            val page = PageImpl(listOf(sampleCourseResponse), PageRequest.of(0, 10), 1)
            whenever(courseService.findAll(any(), any(), any())).thenReturn(page)

            mockMvc.perform(
                get("/courses")
                    .param("name", "Kotlin")
                    .param("active", "true")
                    .contentType(MediaType.APPLICATION_JSON),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Kotlin Basic"))
                .andExpect(jsonPath("$.content[0].description").value("Kotlin Basic description"))
                .andExpect(jsonPath("$.content[0].active").value(true))
                .andExpect(jsonPath("$.totalElements").value(1))
        }
    }


    @Nested
    @DisplayName("GET /courses/{id}")
    inner class FindByIdTests {

        @Test
        @DisplayName("Should return 200 OK with course when found")
        fun shouldReturnCourseWhenFound() {
            whenever(courseService.findById(1L)).thenReturn(sampleCourseResponse)
            mockMvc.perform(
                get("/courses/1")
                    .contentType(MediaType.APPLICATION_JSON),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Kotlin Basic"))
                .andExpect(jsonPath("$.description").value("Kotlin Basic description"))
                .andExpect(jsonPath("$.active").isBoolean())
        }

        @Test
        @DisplayName("Should return 404 Not Found when course does not exist")
        fun shouldReturn404WhenCourseNotFound() {
            whenever(courseService.findById(99L)).thenThrow(CourseNotFoundException(99))
            mockMvc.perform(
                get("/courses/99")
                    .contentType(MediaType.APPLICATION_JSON),
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.message").value("Course with id 99 not found"))

        }
    }

    @Nested
    @DisplayName("PUT /courses/{id}")
    inner class UpdateTests {

        @Test
        @DisplayName("Should return 200 OK when update is successful")
        fun shouldUpdateCourseSuccessfully() {
            val request = UpdateCourseRequest("Kotlin Basic Updated", "Kotlin Basic description", false)
            val updatedResponse = sampleCourseResponse.copy(name = "Kotlin Basic Updated", active = false)
            whenever(courseService.update(1L, request)).thenReturn(updatedResponse)
            mockMvc.perform(
                put("/courses/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("Kotlin Basic Updated"))
                .andExpect(jsonPath("$.active").value(false))
        }

        @Test
        @DisplayName("Should return 404 Not Found when updating non-existing course")
        fun shouldReturn404WhenUpdatingNonExistingCourse() {
            val request = UpdateCourseRequest("Kotlin Basic Updated", "Kotlin Basic description", false)
            whenever(courseService.update(99L, request)).thenThrow(CourseNotFoundException(99))
            mockMvc.perform(
                put("/courses/99")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("DELETE /courses/{id}")
    inner class DeleteTests {

        @Test
        @DisplayName("Should return 204 No Content when deletion is successful")
        fun shouldDeleteCourseSuccessfully() {
            doNothing().whenever(courseService).delete(1L)

            mockMvc.perform(delete("/courses/1"))
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("Should return 404 Not Found when deleting non-existing course")
        fun shouldReturn404WhenDeletingNonExistingCourse() {
            whenever(courseService.delete(99L)).thenThrow(CourseNotFoundException(99L))

            mockMvc.perform(delete("/courses/99"))
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("GET /courses/{id}/students")
    inner class FindStudentsTests {

        @Test
        @DisplayName("Should return 200 OK with list of students for the course")
        fun shouldReturnCourseStudents() {
            val studentsSummary = StudentSummaryResponse(
                id = 10L,
                name = "Test",
            )
            whenever(enrollmentService.findStudentsByCourse(1L)).thenReturn(listOf(studentsSummary))

            mockMvc.perform(get("/courses/1/students"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Test"))
        }
    }

    @Nested
    @DisplayName("POST /courses/batch")
    inner class CreateBatchTests {

        @Test
        @DisplayName("Should return 202 Accepted when batch request is valid")
        fun shouldCreateBatchSuccessfully() {
            val request = CreateCourseBatchRequest(
                courses = listOf(
                    CreateCourseRequest(name = "Course A", description = "This is a description A"),
                    CreateCourseRequest(name = "Course B", description = "This is a description B"),
                ),
            )

            doNothing().whenever(courseService).sendCoursesToQueue(request)

            mockMvc.perform(
                post("/courses/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isAccepted)

            verify(courseService).sendCoursesToQueue(request)
        }

        @Test
        @DisplayName("Should return 400 Bad Request when batch request is invalid")
        fun shouldReturn400WhenBatchRequestIsInvalid() {
            val invalidRequest = CreateCourseBatchRequest(courses = emptyList())

            mockMvc.perform(
                post("/courses/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.fields").isArray)
        }
    }
}
