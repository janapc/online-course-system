package com.janapc.online_course_system.course.exception

import com.janapc.online_course_system.common.exception.NotFoundException

class CourseNotFoundException(
	id: Long,
) : NotFoundException("Course with id $id not found")
