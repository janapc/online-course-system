package com.janapc.online_course_system.common.exception

abstract class UnauthorizedException(
    message: String,
) : RuntimeException(message) {
}
