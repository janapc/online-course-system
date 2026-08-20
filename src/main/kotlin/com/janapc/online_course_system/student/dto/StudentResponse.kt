package com.janapc.online_course_system.student.dto

import java.time.LocalDateTime

data class StudentResponse(
	val id: Long,
	val name: String,
	val email: String,
	val active: Boolean,
	val createdAt: LocalDateTime?,
	val updatedAt: LocalDateTime?,
)
