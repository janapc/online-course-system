package com.janapc.online_course_system.enrollment.dto

import java.time.LocalDateTime

data class EnrollmentResponse(
    val id: Long,
    val studentId: Long,
    val courseId: Long,
    val enrolledAt: LocalDateTime,
    val active: Boolean,
)
