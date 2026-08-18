package com.janapc.online_course_system.student.controller

import com.janapc.online_course_system.course.dto.CourseSummaryResponse
import com.janapc.online_course_system.enrollment.service.EnrollmentService
import com.janapc.online_course_system.student.dto.CreateStudentBatchRequest
import com.janapc.online_course_system.student.dto.CreateStudentRequest
import com.janapc.online_course_system.student.dto.StudentResponse
import com.janapc.online_course_system.student.dto.UpdateStudentRequest
import com.janapc.online_course_system.student.service.StudentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Students", description = "Endpoints for managing student profiles and records")
@RestController
@RequestMapping("/students")
class StudentController(
    private val studentService: StudentService,
    private val enrollmentService: EnrollmentService,
) {

    @Operation(summary = "List all students with optional filters")
    @GetMapping
    fun findAll(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) active: Boolean?,
        @PageableDefault(page = 0, size = 10, sort = ["id"]) pageable: Pageable,
    ): Page<StudentResponse> {
        return studentService.findAll(name, active, pageable)
    }

    @Operation(summary = "Create a new student (ADMIN only)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody student: CreateStudentRequest): StudentResponse {
        return studentService.create(student)
    }

    @Operation(summary = "Get student by ID")
    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): StudentResponse {
        return studentService.findById(id)
    }

    @Operation(summary = "Update an existing student (ADMIN only)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody student: UpdateStudentRequest,
    ): ResponseEntity<StudentResponse> {
        val updatedStudent = studentService.update(id, student)
        return if (updatedStudent != null) {
            ResponseEntity.ok(updatedStudent)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @Operation(summary = "Delete a student (ADMIN only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: Long): ResponseEntity<Unit> {
        studentService.delete(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @Operation(summary = "Get enrolled courses of a student")
    @GetMapping("/{id}/courses")
    fun findCourses(@PathVariable id: Long): List<CourseSummaryResponse> {
        return enrollmentService.findCoursesByStudent(id)
    }

    @Operation(summary = "Enqueue a batch of students for creation (ADMIN only)")
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('ADMIN')")
    fun createBatch(
        @Valid @RequestBody requests: CreateStudentBatchRequest,
    ) {
        studentService.sendStudentsToQueue(requests)
    }
}
