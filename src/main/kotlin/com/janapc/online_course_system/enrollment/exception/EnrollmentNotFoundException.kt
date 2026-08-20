package com.janapc.online_course_system.enrollment.exception

import com.janapc.online_course_system.common.exception.NotFoundException

class EnrollmentNotFoundException(
	val id: Long,
) : NotFoundException("Enrollment not found with id $id")
