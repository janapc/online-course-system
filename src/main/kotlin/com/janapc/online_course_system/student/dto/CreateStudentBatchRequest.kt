package com.janapc.online_course_system.student.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

data class CreateStudentBatchRequest(
	@field:NotEmpty(message = "The student list cannot be empty")
	@field:Valid
	val students: List<CreateStudentRequest>,
)
