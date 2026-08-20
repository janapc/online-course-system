package com.janapc.online_course_system.student.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateStudentRequest(
	@field:NotBlank(message = "Name is required")
	@field:Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
	val name: String,
	@field:NotBlank(message = "Email is required")
	@field:Email(message = "Email is wrong")
	val email: String,
)
