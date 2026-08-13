package com.janapc.online_course_system.enrollment.controller

import com.janapc.online_course_system.enrollment.dto.CreateEnrollmentRequest
import com.janapc.online_course_system.enrollment.dto.EnrollmentDetailsResponse
import com.janapc.online_course_system.enrollment.dto.EnrollmentResponse
import com.janapc.online_course_system.enrollment.service.EnrollmentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Enrollments", description = "Endpoints for managing course enrollments")
@RestController
@RequestMapping("/enrollments")
class EnrollmentController(
    private val enrollmentService: EnrollmentService,
) {
    @Operation(summary = "Enroll a student in a course (ADMIN only)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateEnrollmentRequest): EnrollmentResponse {
        return enrollmentService.create(request)
    }

    @Operation(summary = "List all enrollments with pagination")
    @GetMapping
    fun findAll(pageable: Pageable): Page<EnrollmentDetailsResponse> {
        return enrollmentService.findAll(pageable)
    }

    @Operation(summary = "Get enrollment details by ID")
    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): EnrollmentDetailsResponse {
        return enrollmentService.findById(id)
    }

    @Operation(summary = "Cancel an enrollment (ADMIN only)")
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    fun cancel(@PathVariable id: Long): EnrollmentResponse {
        return enrollmentService.cancel(id)
    }
}
