package com.janapc.online_course_system.common.exception

abstract class ConflictException(
    message: String,
) : RuntimeException(message) {
}
