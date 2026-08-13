package com.janapc.online_course_system.enrollment.repository

import com.janapc.online_course_system.course.entity.Course
import com.janapc.online_course_system.enrollment.entity.Enrollment
import com.janapc.online_course_system.student.entity.Student
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
class EnrollmentRepositoryTest {
    @Autowired
    private lateinit var enrollmentRepository: EnrollmentRepository


    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Nested
    @DisplayName("Test Find By StudentId And Active True")
    inner class FindByStudentIdAndActiveTrue {

        @Test
        @DisplayName("Should return list of enrollments find by studentId and enrollments activated")
        fun shouldReturnListEnrollmentsByStudentIdAndActiveTrue() {
            val student = Student(name = "test", email = "test@test.com", active = true)
            val studentId = entityManager.persist(student).id
            val course = Course(
                name = "Kotlin Architecture 101",
                description = "Learn Clean Architecture and Spring Boot",
                active = true,
            )
            entityManager.persist(course)
            val enrollment = Enrollment(student = student, course = course, active = true)
            entityManager.persist(enrollment)
            val result = enrollmentRepository.findByStudentIdAndActiveTrue(studentId!!)
            assertNotNull(result)
            assertEquals(1, result.size)
            assertNotNull(result.first().id)
            assertTrue(result.first().active)
        }

        @Test
        @DisplayName("Should return an empty list of enrollments when the enrollment is inactive for that student.")
        fun shouldReturnEmptyListInEnrollmentsWhenEnrollmentIsInactiveForStudent() {
            val student = Student(name = "test", email = "test@test.com", active = true)
            val studentId = entityManager.persist(student).id
            val course = Course(
                name = "Kotlin Architecture 101",
                description = "Learn Clean Architecture and Spring Boot",
                active = true,
            )
            entityManager.persist(course)
            val enrollment = Enrollment(student = student, course = course, active = false)
            entityManager.persist(enrollment)
            val result = enrollmentRepository.findByStudentIdAndActiveTrue(studentId!!)
            assertEquals(0, result.size)
        }
    }

    @Nested
    @DisplayName("Test Find By CourseId And Active True")
    inner class FindByCourseIdAndActiveTrue {

        @Test
        @DisplayName("Should return list of enrollments find by courseId and enrollments activated")
        fun shouldReturnListEnrollmentsByCourseIdAndActiveTrue() {
            val student = Student(name = "test", email = "test@test.com", active = true)
            entityManager.persist(student)
            val course = Course(
                name = "Kotlin Architecture 101",
                description = "Learn Clean Architecture and Spring Boot",
                active = true,
            )
            val courseId = entityManager.persist(course).id
            val enrollment = Enrollment(student = student, course = course, active = true)
            entityManager.persist(enrollment)
            val result = enrollmentRepository.findByCourseIdAndActiveTrue(courseId!!)
            assertNotNull(result)
            assertEquals(1, result.size)
            assertNotNull(result.first().id)
            assertTrue(result.first().active)
        }

        @Test
        @DisplayName("Should return an empty list of enrollments when the enrollment is inactive for that course")
        fun shouldReturnEmptyListInEnrollmentsWhenEnrollmentIsInactiveForCourse() {
            val student = Student(name = "test", email = "test@test.com", active = true)
            entityManager.persist(student)
            val course = Course(
                name = "Kotlin Architecture 101",
                description = "Learn Clean Architecture and Spring Boot",
                active = true,
            )
            val courseId = entityManager.persist(course).id
            val enrollment = Enrollment(student = student, course = course, active = false)
            entityManager.persist(enrollment)
            val result = enrollmentRepository.findByCourseIdAndActiveTrue(courseId!!)
            assertEquals(0, result.size)
        }
    }

    @Nested
    @DisplayName("Test Find By StudentId And CourseId")
    inner class FindByStudentIdAndCourseId {
        @Test
        @DisplayName("Should return enrollment by course id and student id")
        fun shouldReturnEnrollmentByCourseIdAndStudentId() {
            val student = Student(name = "test", email = "test@test.com", active = true)
            val studentId = entityManager.persist(student).id
            val course = Course(
                name = "Kotlin Architecture 101",
                description = "Learn Clean Architecture and Spring Boot",
                active = true,
            )
            val courseId = entityManager.persist(course).id
            val enrollment = Enrollment(student = student, course = course, active = true)
            entityManager.persist(enrollment)
            val result = enrollmentRepository.findByStudentIdAndCourseId(studentId!!, courseId!!)
            assertNotNull(result)
            assertNotNull(result.id)
            assertTrue(result.active)
        }

        @Test
        @DisplayName("Should return a blank value if the enrollment is not linked to that course or student")
        fun shouldReturnEmptyWhenEnrollmentIsLinkedToTheCourseOrStudent() {
            val student = Student(name = "test", email = "test@test.com", active = true)
            val studentId = entityManager.persist(student).id
            val course = Course(
                name = "Kotlin Architecture 101",
                description = "Learn Clean Architecture and Spring Boot",
                active = true,
            )
            entityManager.persist(course)
            val enrollment = Enrollment(student = student, course = course, active = true)
            entityManager.persist(enrollment)
            val result = enrollmentRepository.findByStudentIdAndCourseId(studentId!!, 9999L)
            assertNull(result)
        }
    }

}
