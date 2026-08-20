package com.janapc.online_course_system.enrollment.mapper

import com.janapc.online_course_system.course.mapper.CourseMapper
import com.janapc.online_course_system.enrollment.dto.EnrollmentDetailsResponse
import com.janapc.online_course_system.enrollment.dto.EnrollmentResponse
import com.janapc.online_course_system.enrollment.entity.Enrollment
import com.janapc.online_course_system.student.mapper.StudentMapper

object EnrollmentMapper {
	fun toResponse(enrollment: Enrollment): EnrollmentResponse =
		EnrollmentResponse(
			id = enrollment.id!!,
			studentId = enrollment.student.id!!,
			courseId = enrollment.course.id!!,
			enrolledAt = enrollment.enrolledAt,
			active = enrollment.active,
		)

	fun toDetailsResponse(enrollment: Enrollment): EnrollmentDetailsResponse =
		EnrollmentDetailsResponse(
			id = enrollment.id!!,
			student = StudentMapper.toSummaryResponse(enrollment.student),
			course = CourseMapper.toSummaryResponse(enrollment.course),
			active = enrollment.active,
			enrolledAt = enrollment.enrolledAt,
			createdAt = enrollment.createdAt,
			updatedAt = enrollment.updatedAt,
		)
}
