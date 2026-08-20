package com.janapc.online_course_system.security.repository

import com.janapc.online_course_system.security.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
	fun findByEmail(email: String): User?
}
