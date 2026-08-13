package com.janapc.online_course_system.enrollment.exception

import com.janapc.online_course_system.common.exception.ConflictException

class EnrollmentAlreadyExistsException :
    ConflictException("Student already exists in this course")
