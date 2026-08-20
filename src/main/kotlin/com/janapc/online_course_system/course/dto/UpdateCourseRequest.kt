package com.janapc.online_course_system.course.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateCourseRequest(
	@field:NotBlank
	@field:Size(min = 3, max = 100)
	val name: String,
	@field:NotBlank
	@field:Size(max = 1000)
	val description: String,
	val active: Boolean,
)
