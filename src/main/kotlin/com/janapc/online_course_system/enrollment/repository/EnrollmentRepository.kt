package com.janapc.online_course_system.enrollment.repository

import com.janapc.online_course_system.enrollment.entity.Enrollment
import org.springframework.data.jpa.repository.JpaRepository

interface EnrollmentRepository : JpaRepository<Enrollment, Long> {
	fun findByStudentIdAndActiveTrue(studentId: Long): List<Enrollment>

	fun findByCourseIdAndActiveTrue(courseId: Long): List<Enrollment>

	fun findByStudentIdAndCourseId(
		studentId: Long,
		courseId: Long,
	): Enrollment?
}
