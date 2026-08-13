package com.janapc.online_course_system.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.janapc.online_course_system.course.dto.CreateCourseRequest
import com.janapc.online_course_system.security.dto.AuthResponse
import com.janapc.online_course_system.security.dto.LoginRequest
import com.janapc.online_course_system.security.dto.RegisterRequest
import com.janapc.online_course_system.security.entity.Role
import com.janapc.online_course_system.student.dto.CreateStudentRequest
import com.janapc.online_course_system.student.dto.UpdateStudentRequest
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RbacE2ETest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private fun MockHttpServletRequestBuilder.bearer(token: String): MockHttpServletRequestBuilder =
        this.header(HttpHeaders.AUTHORIZATION, "Bearer $token")

    private fun MockHttpServletRequestBuilder.json(body: Any): MockHttpServletRequestBuilder =
        this.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body))

    private fun obtainAccessToken(
        email: String,
        password: String = "password123",
        role: Role = Role.USER,
    ): String {
        val registerRequest = RegisterRequest(email = email, password = password, role = role)
        mockMvc.perform(post("/auth/register").json(registerRequest))

        val loginResult = mockMvc.perform(
            post("/auth/login").json(LoginRequest(email = email, password = password)),
        ).andExpect(status().isOk).andReturn()

        val response = objectMapper.readValue(
            loginResult.response.contentAsString,
            AuthResponse::class.java,
        )
        return response.token
    }

    @Nested
    @DisplayName("ROLE_USER Access Restrictions (403 Forbidden)")
    inner class RoleUserRestrictionTests {

        @Test
        @DisplayName("Should return 403 Forbidden when ROLE_USER attempts to create a student")
        fun shouldReturn403WhenUserTriesToCreateStudent() {
            val userToken = obtainAccessToken(email = "regular_user_create@test.com", role = Role.USER)
            val request = CreateStudentRequest(name = "Forbidden Student", email = "forbidden_student@test.com")

            mockMvc.perform(
                post("/students")
                    .bearer(userToken)
                    .json(request),
            )
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("Should return 403 Forbidden when ROLE_USER attempts to update a student")
        fun shouldReturn403WhenUserTriesToUpdateStudent() {
            val userToken = obtainAccessToken(email = "regular_user_update@test.com", role = Role.USER)
            val request =
                UpdateStudentRequest(name = "Updated Name", email = "regular_user_update@test.com", active = true)

            mockMvc.perform(
                put("/students/1")
                    .bearer(userToken)
                    .json(request),
            )
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("Should return 403 Forbidden when ROLE_USER attempts to delete a student")
        fun shouldReturn403WhenUserTriesToDeleteStudent() {
            val userToken = obtainAccessToken(email = "regular_user_delete@test.com", role = Role.USER)

            mockMvc.perform(
                delete("/students/1").bearer(userToken),
            )
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("Should return 403 Forbidden when ROLE_USER attempts to create a course")
        fun shouldReturn403WhenUserTriesToCreateCourse() {
            val userToken = obtainAccessToken(email = "regular_user_course@test.com", role = Role.USER)
            val request = CreateCourseRequest(name = "Forbidden Course", description = "Course Description")

            mockMvc.perform(
                post("/courses")
                    .bearer(userToken)
                    .json(request),
            )
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("Should return 200 OK when ROLE_USER attempts to read students")
        fun shouldAllowUserToReadStudents() {
            val userToken = obtainAccessToken(email = "regular_user_read@test.com", role = Role.USER)

            mockMvc.perform(
                get("/students").bearer(userToken),
            )
                .andExpect(status().isOk)
        }
    }

    @Nested
    @DisplayName("ROLE_ADMIN Access Permissions (Allowed)")
    inner class RoleAdminPermissionTests {

        @Test
        @DisplayName("Should return 201 Created when ROLE_ADMIN creates a student")
        fun shouldAllowAdminToCreateStudent() {
            val adminToken = obtainAccessToken(email = "admin_user_create@test.com", role = Role.ADMIN)
            val request = CreateStudentRequest(name = "Allowed Student", email = "allowed_student@test.com")

            mockMvc.perform(
                post("/students")
                    .bearer(adminToken)
                    .json(request),
            )
                .andExpect(status().isCreated)
        }
    }
}
