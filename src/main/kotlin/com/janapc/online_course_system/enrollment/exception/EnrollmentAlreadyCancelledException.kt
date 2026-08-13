package com.janapc.online_course_system.enrollment.exception

import com.janapc.online_course_system.common.exception.ConflictException

class EnrollmentAlreadyCancelledException(id: Long) : ConflictException("Enrollment with id $id has been cancelled") {
}
