package com.janapc.online_course_system.student.repository

import com.janapc.online_course_system.student.entity.Student
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface StudentRepository :
	JpaRepository<Student, Long>,
	JpaSpecificationExecutor<Student> {
	fun existsByEmail(email: String): Boolean

	fun findByEmail(email: String): Student?
}
