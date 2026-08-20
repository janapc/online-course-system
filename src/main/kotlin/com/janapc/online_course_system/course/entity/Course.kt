package com.janapc.online_course_system.course.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

@Entity
@Table(name = "courses")
class Course(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	var id: Long? = null,
	@Column(nullable = false)
	var name: String,
	@Column(nullable = false, length = 1000)
	var description: String,
	@Column(nullable = false)
	var active: Boolean = true,
	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	var createdAt: LocalDateTime? = null,
	@UpdateTimestamp
	@Column(nullable = false)
	var updatedAt: LocalDateTime? = null,
)
