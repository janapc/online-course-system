package com.janapc.online_course_system.security.repository

import com.janapc.online_course_system.security.entity.Role
import com.janapc.online_course_system.security.entity.User
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
	@Autowired
	private lateinit var userRepository: UserRepository

	@Autowired
	private lateinit var entityManager: TestEntityManager

	@Nested
	@DisplayName("Test Find By Email")
	inner class FindByEmail {
		@Test
		@DisplayName("Should return user when email exists")
		fun shouldReturnUserWhenEmailExists() {
			val user = User(email = "john@test.com", password = "password", role = Role.USER)
			entityManager.persist(user)
			val result = userRepository.findByEmail(user.email)
			assertNotNull(result)
			assertEquals("john@test.com", result?.email)
		}

		@Test
		@DisplayName("Should return empty when email not exists")
		fun shouldReturnEmptyWhenEmailNotExists() {
			val result = userRepository.findByEmail("noexists@email.com")
			assertNull(result)
		}
	}
}
