package com.janapc.online_course_system.course.mapper

import com.janapc.online_course_system.course.dto.CourseResponse
import com.janapc.online_course_system.course.dto.CourseSummaryResponse
import com.janapc.online_course_system.course.entity.Course

object CourseMapper {
	fun toResponse(course: Course): CourseResponse =
		CourseResponse(
			id = course.id!!,
			name = course.name,
			description = course.description,
			active = course.active,
			createdAt = course.createdAt,
			updatedAt = course.updatedAt,
		)

	fun toSummaryResponse(course: Course): CourseSummaryResponse =
		CourseSummaryResponse(
			id = course.id!!,
			name = course.name,
		)
}
