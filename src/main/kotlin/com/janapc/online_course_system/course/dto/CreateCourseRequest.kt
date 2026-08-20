package com.janapc.online_course_system.course.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateCourseRequest(
	@field:NotBlank
	@field:Size(min = 3, max = 100)
	val name: String,
	@field:NotBlank
	@field:Size(max = 100)
	val description: String,
)
