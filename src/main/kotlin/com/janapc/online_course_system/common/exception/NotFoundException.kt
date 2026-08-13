package com.janapc.online_course_system.common.exception

abstract class NotFoundException(
    message: String,
) : RuntimeException(message) {
}
