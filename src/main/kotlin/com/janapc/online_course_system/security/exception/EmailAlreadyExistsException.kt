package com.janapc.online_course_system.security.exception

import com.janapc.online_course_system.common.exception.ConflictException

class EmailAlreadyExistsException : ConflictException("Email already registered") {
}
