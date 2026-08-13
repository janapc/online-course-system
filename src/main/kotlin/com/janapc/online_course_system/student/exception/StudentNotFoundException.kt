package com.janapc.online_course_system.student.exception

import com.janapc.online_course_system.common.exception.NotFoundException

class StudentNotFoundException(
    id: Long,
) : NotFoundException("Student with id $id not found")
