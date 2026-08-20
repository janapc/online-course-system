package com.janapc.online_course_system.common.exception

data class FieldErrorResponse(
	val field: String,
	val message: String,
)
