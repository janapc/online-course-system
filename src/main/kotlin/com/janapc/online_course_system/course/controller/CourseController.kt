package com.janapc.online_course_system.course.controller

import com.janapc.online_course_system.course.dto.CourseResponse
import com.janapc.online_course_system.course.dto.CreateCourseRequest
import com.janapc.online_course_system.course.dto.UpdateCourseRequest
import com.janapc.online_course_system.course.service.CourseService
import com.janapc.online_course_system.enrollment.service.EnrollmentService
import com.janapc.online_course_system.student.dto.StudentSummaryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Courses", description = "Endpoints for managing course details and enrolled students")
@RestController
@RequestMapping("/courses")
class CourseController(
    private val courseService: CourseService,
    private val enrollmentService: EnrollmentService,
) {

    @Operation(summary = "Create a new course (ADMIN only)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateCourseRequest): CourseResponse {
        return courseService.create(request)
    }

    @Operation(summary = "List all courses with optional filters")
    @GetMapping
    fun findAll(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) active: Boolean?,
        pageable: Pageable,
    ): Page<CourseResponse> {
        return courseService.findAll(name, active, pageable)
    }

    @Operation(summary = "Get course by ID")
    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): CourseResponse {
        return courseService.findById(id)
    }

    @Operation(summary = "Update an existing course (ADMIN only)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdateCourseRequest): CourseResponse {
        return courseService.update(id, request)
    }

    @Operation(summary = "Delete a course (ADMIN only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        courseService.delete(id)
    }

    @Operation(summary = "Get enrolled students of a course")
    @GetMapping("/{id}/students")
    fun findStudents(@PathVariable id: Long): List<StudentSummaryResponse> {
        return enrollmentService.findStudentsByCourse(id)
    }
}
