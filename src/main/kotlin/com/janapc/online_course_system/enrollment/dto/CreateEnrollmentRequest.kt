package com.janapc.online_course_system.enrollment.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive


data class CreateEnrollmentRequest(
    @field:NotNull(message = "StudentId is mandatory")
    @field:Positive(message = "StudentId must be positive number")
    val studentId: Long?,

    @field:NotNull(message = "CourseId is mandatory")
    @field:Positive(message = "CourseId must be positive number")
    val courseId: Long?,
)
