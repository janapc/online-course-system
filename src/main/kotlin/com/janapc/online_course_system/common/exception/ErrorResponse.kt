package com.janapc.online_course_system.common.exception

import java.time.LocalDateTime

data class ErrorResponse(
	val message: String,
	val status: Int,
	val error: String,
	val timestamp: LocalDateTime = LocalDateTime.now(),
	val path: String,
	val fields: List<FieldErrorResponse> = emptyList(),
)
