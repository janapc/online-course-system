package com.janapc.online_course_system.student.exception

import com.janapc.online_course_system.common.exception.ConflictException

class StudentAlreadyExistsException : ConflictException("Student with this email already exists") {
}
