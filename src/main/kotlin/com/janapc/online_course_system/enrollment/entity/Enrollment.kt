package com.janapc.online_course_system.enrollment.entity

import com.janapc.online_course_system.course.entity.Course
import com.janapc.online_course_system.student.entity.Student
import jakarta.persistence.*
import java.time.LocalDateTime
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

@Entity
@Table(
	name = "enrollments",
	uniqueConstraints = [
		UniqueConstraint(
			columnNames = ["student_id", "course_id"],
		),
	],
)
class Enrollment(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	var id: Long? = null,
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id")
	var student: Student,
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id")
	var course: Course,
	@Column(nullable = false)
	var enrolledAt: LocalDateTime = LocalDateTime.now(),
	@Column(nullable = false)
	var active: Boolean = true,
	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	var createdAt: LocalDateTime? = null,
	@UpdateTimestamp
	@Column(nullable = false)
	var updatedAt: LocalDateTime? = null,
)
