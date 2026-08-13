package com.janapc.online_course_system.enrollment.dto

import com.janapc.online_course_system.course.dto.CourseSummaryResponse
import com.janapc.online_course_system.student.dto.StudentSummaryResponse
import java.time.LocalDateTime

data class EnrollmentDetailsResponse(
    val id: Long,
    val student: StudentSummaryResponse,
    val course: CourseSummaryResponse,
    val active: Boolean,
    val enrolledAt: LocalDateTime,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
)
