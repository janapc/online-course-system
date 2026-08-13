package com.janapc.online_course_system.course

import com.fasterxml.jackson.databind.ObjectMapper
import com.janapc.online_course_system.course.dto.CourseResponse
import com.janapc.online_course_system.course.dto.CreateCourseRequest
import com.janapc.online_course_system.course.dto.UpdateCourseRequest
import com.janapc.online_course_system.security.dto.AuthResponse
import com.janapc.online_course_system.security.dto.LoginRequest
import com.janapc.online_course_system.security.dto.RegisterRequest
import com.janapc.online_course_system.security.entity.Role
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CourseE2ETest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private fun MockHttpServletRequestBuilder.bearer(token: String): MockHttpServletRequestBuilder =
        this.header(HttpHeaders.AUTHORIZATION, "Bearer $token")

    private fun MockHttpServletRequestBuilder.json(body: Any): MockHttpServletRequestBuilder =
        this.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body))

    private fun obtainAccessToken(
        email: String = "course_e2e@test.com",
        password: String = "password123",
        role: Role = Role.ADMIN,
    ): String {
        mockMvc.perform(post("/auth/register").json(RegisterRequest(email, password, role)))

        val loginResult = mockMvc.perform(
            post("/auth/login").json(LoginRequest(email, password)),
        ).andExpect(status().isOk).andReturn()

        val response = objectMapper.readValue(
            loginResult.response.contentAsString,
            AuthResponse::class.java,
        )
        return response.token
    }

    @Nested
    @DisplayName("Course Operations & Lifecycle")
    inner class CourseOperationsTests {

        @Test
        @DisplayName("Should create, find, update and delete a course successfully")
        fun shouldExecuteFullCourseLifecycle() {
            val token = obtainAccessToken()
            val createCourseRequest = CreateCourseRequest(
                name = "Kotlin Architecture 101",
                description = "Learn Clean Architecture and Spring Boot",
            )

            val courseResult = mockMvc.perform(
                post("/courses")
                    .bearer(token)
                    .json(createCourseRequest),
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.name").value("Kotlin Architecture 101"))
                .andExpect(jsonPath("$.description").value("Learn Clean Architecture and Spring Boot"))
                .andReturn()
            val courseId = objectMapper.readValue(
                courseResult.response.contentAsString,
                CourseResponse::class.java,
            ).id

            mockMvc.perform(
                get("/courses/$courseId").bearer(token),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(courseId))
                .andExpect(jsonPath("$.name").value("Kotlin Architecture 101"))

            val updateRequest = UpdateCourseRequest(
                name = "Advanced Kotlin Architecture",
                description = "Updated description for Advanced topics",
                active = false,
            )

            mockMvc.perform(
                put("/courses/$courseId")
                    .bearer(token)
                    .json(updateRequest),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("Advanced Kotlin Architecture"))
                .andExpect(jsonPath("$.active").value(false))

            mockMvc.perform(
                delete("/courses/$courseId").bearer(token),
            )
                .andExpect(status().isNoContent)

            mockMvc.perform(
                get("/courses/$courseId").bearer(token),
            )
                .andExpect(status().isNotFound)
        }

        @Test
        @DisplayName("Should filter courses by name and status with pagination")
        fun shouldFilterCoursesWithPagination() {
            val token = obtainAccessToken()

            val createCourseRequest = CreateCourseRequest(
                name = "Docker for Developers",
                description = "Containerization basics",
            )
            mockMvc.perform(post("/courses").bearer(token).json(createCourseRequest))
                .andExpect(status().isCreated)

            mockMvc.perform(
                get("/courses")
                    .bearer(token)
                    .param("name", "Docker")
                    .param("active", "true")
                    .param("page", "0")
                    .param("size", "5"),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isArray)
                .andExpect(jsonPath("$.pageable.pageSize").value(5))
        }
    }

    @Nested
    @DisplayName("Validation")
    inner class ValidationTests {

        @Test
        @DisplayName("Should return 400 Bad Request when creating course with invalid data")
        fun shouldReturn400WhenDataIsInvalid() {
            val token = obtainAccessToken()
            val invalidRequest = CreateCourseRequest(name = "", description = "")

            mockMvc.perform(
                post("/courses")
                    .bearer(token)
                    .json(invalidRequest),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.fields").isArray)
        }

        @Test
        @DisplayName("Should return 404 Not Found when searching for non-existing course")
        fun shouldReturn404ForMissingCourse() {
            val token = obtainAccessToken()

            mockMvc.perform(get("/courses/999999").bearer(token))
                .andExpect(status().isNotFound)
        }
    }
}
