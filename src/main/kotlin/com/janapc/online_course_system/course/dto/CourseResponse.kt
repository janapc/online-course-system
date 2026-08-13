package com.janapc.online_course_system.course.dto

import java.time.LocalDateTime

data class CourseResponse(
    val id: Long,
    val name: String,
    val description: String,
    val active: Boolean,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
)
