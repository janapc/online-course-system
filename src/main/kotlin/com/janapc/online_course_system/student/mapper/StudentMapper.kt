package com.janapc.online_course_system.student.mapper

import com.janapc.online_course_system.student.dto.StudentResponse
import com.janapc.online_course_system.student.dto.StudentSummaryResponse
import com.janapc.online_course_system.student.entity.Student

object StudentMapper {
    fun toResponse(student: Student): StudentResponse = StudentResponse(
        id = student.id!!,
        name = student.name,
        email = student.email,
        active = student.active,
        createdAt = student.createdAt,
        updatedAt = student.updatedAt,
    )

    fun toSummaryResponse(student: Student): StudentSummaryResponse = StudentSummaryResponse(
        id = student.id!!,
        name = student.name,
    )
}
