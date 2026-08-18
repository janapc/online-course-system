package com.janapc.online_course_system.course.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

data class CreateCourseBatchRequest(
    @field:NotEmpty(message = "The course list cannot be empty")
    @field:Valid
    val courses: List<CreateCourseRequest>,
) {
}
