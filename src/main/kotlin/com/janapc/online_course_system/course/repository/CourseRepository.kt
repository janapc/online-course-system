package com.janapc.online_course_system.course.repository

import com.janapc.online_course_system.course.entity.Course
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface CourseRepository :
	JpaRepository<Course, Long>,
	JpaSpecificationExecutor<Course>
