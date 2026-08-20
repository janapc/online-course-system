package com.janapc.online_course_system.student.repository

import com.janapc.online_course_system.student.entity.Student
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
class StudentRepositoryTest {
	@Autowired
	private lateinit var studentRepository: StudentRepository

	@Autowired
	private lateinit var entityManager: TestEntityManager

	@Nested
	@DisplayName("Email Queries")
	inner class EmailQueries {
		@Test
		@DisplayName("Should return true when email exists in database")
		fun shouldReturnTrueWhenEmailExists() {
			val student = Student(name = "John", email = "john@test.com", active = true)
			entityManager.persist(student)
			val result = studentRepository.existsByEmail(student.email)
			assertTrue(result)
		}

		@Test
		@DisplayName("Should return false when email does not exist in database")
		fun shouldReturnFalseWhenEmailDoesNotExist() {
			val result = studentRepository.existsByEmail("nonexistent@test.com")
			assertFalse(result)
		}

		@Test
		@DisplayName("Should find student by email")
		fun shouldFindStudentByEmail() {
			val student = Student(name = "John", email = "john@test.com", active = true)
			entityManager.persist(student)
			val result = studentRepository.findByEmail(student.email)
			assertNotNull(result)
			assertEquals("John", result?.name)
			assertEquals("john@test.com", result?.email)
		}
	}

	@Nested
	@DisplayName("Database Constraints")
	inner class ConstraintTests {
		@Test
		@DisplayName("Should throw DataIntegrityViolationException when inserting duplicate email")
		fun shouldThrowExceptionOnDuplicateEmail() {
			val student1 = Student(name = "Maria", email = "unique@test.com", active = true)
			val student2 = Student(name = "João", email = "unique@test.com", active = true)

			studentRepository.saveAndFlush(student1)
			assertThrows(DataIntegrityViolationException::class.java) {
				studentRepository.saveAndFlush(student2)
			}
		}
	}
}
