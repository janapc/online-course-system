package com.janapc.online_course_system.enrollment.service

import com.janapc.online_course_system.course.dto.CourseSummaryResponse
import com.janapc.online_course_system.course.entity.Course
import com.janapc.online_course_system.course.exception.CourseNotFoundException
import com.janapc.online_course_system.course.repository.CourseRepository
import com.janapc.online_course_system.enrollment.dto.CreateEnrollmentRequest
import com.janapc.online_course_system.enrollment.entity.Enrollment
import com.janapc.online_course_system.enrollment.exception.EnrollmentAlreadyExistsException
import com.janapc.online_course_system.enrollment.exception.EnrollmentNotFoundException
import com.janapc.online_course_system.enrollment.repository.EnrollmentRepository
import com.janapc.online_course_system.student.dto.StudentSummaryResponse
import com.janapc.online_course_system.student.entity.Student
import com.janapc.online_course_system.student.exception.StudentNotFoundException
import com.janapc.online_course_system.student.repository.StudentRepository
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

@ExtendWith(MockitoExtension::class)
class EnrollmentServiceTest {
    @Mock
    lateinit var enrollmentRepository: EnrollmentRepository

    @Mock
    lateinit var studentRepository: StudentRepository

    @Mock
    lateinit var courseRepository: CourseRepository

    @InjectMocks
    lateinit var enrollmentService: EnrollmentService

    private fun generateEnrollment(): Enrollment = Enrollment(
        id = 1L,
        course = generateCourse(),
        student = generateStudent(),
        enrolledAt = LocalDateTime.now(),
        active = true,
    )

    private fun generateCourse(): Course = Course(
        id = 2L,
        name = "Test Course",
        description = "Test description",
    )

    private fun generateStudent(): Student = Student(
        id = 3L,
        name = "Test Student",
        email = "test@email.com",
    )

    @Nested
    inner class FindAll {
        @Test
        fun `should find all enrollment`() {
            val enrollment = generateEnrollment()
            val pageable = PageRequest.of(0, 10)
            val page = PageImpl<Enrollment>(listOf(enrollment))
            whenever(enrollmentRepository.findAll(eq(pageable))).thenReturn(page)
            val response = enrollmentService.findAll(pageable)
            assertEquals(1, response.totalElements)
            assertEquals(1L, response.content.first().id)
            assertTrue(response.content.first().active)
            assertNotNull(response.content.first().course)
            assertNotNull(response.content.first().student)
            assertNotNull(response.content.first().enrolledAt)
            verify(enrollmentRepository, times(1)).findAll(eq(pageable))
        }
    }

    @Nested
    inner class FindById {
        @Test
        fun `should find enrollment by id`() {
            val enrollment = generateEnrollment()
            whenever(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment))
            val response = enrollmentService.findById(1L)
            assertEquals(1L, response.id)
            assertTrue(response.active)
            assertNotNull(response.course)
            assertNotNull(response.student)
            assertInstanceOf<StudentSummaryResponse>(response.student)
            assertInstanceOf<CourseSummaryResponse>(response.course)
            assertNotNull(response.enrolledAt)
            verify(enrollmentRepository, times(1)).findById(1L)
        }

        @Test
        fun `should throw exception when course not found`() {
            whenever(enrollmentRepository.findById(1L)).thenReturn(Optional.empty())
            assertThrows<EnrollmentNotFoundException> {
                enrollmentService.findById(1L)
            }
            verify(enrollmentRepository, times(1)).findById(1L)
        }
    }

    @Nested
    inner class CreateEnrollment {
        @Test
        fun `should create a new enrollment`() {
            val request = CreateEnrollmentRequest(
                studentId = 3L,
                courseId = 2L,
            )
            val student = generateStudent()
            val course = generateCourse()
            val enrollment = generateEnrollment()
            whenever(enrollmentRepository.findByStudentIdAndCourseId(3L, 2L)).thenReturn(null)
            whenever(studentRepository.findById(3L)).thenReturn(Optional.of(student))
            whenever(courseRepository.findById(2L)).thenReturn(Optional.of(course))
            whenever(enrollmentRepository.save(any())).thenReturn(enrollment)
            val response = enrollmentService.create(request)
            assertEquals(1L, response.id)
            assertTrue(response.active)
            verify(enrollmentRepository).findByStudentIdAndCourseId(3L, 2L)
            verify(studentRepository).findById(3L)
            verify(courseRepository).findById(2L)
            verify(enrollmentRepository, times(1)).save(any())
        }

        @Test
        fun `should reactive enrollment`() {
            val enrollment = generateEnrollment()
            enrollment.active = false
            val request = CreateEnrollmentRequest(
                studentId = 3L,
                courseId = 2L,
            )
            whenever(enrollmentRepository.findByStudentIdAndCourseId(3L, 2L)).thenReturn(enrollment)
            whenever(enrollmentRepository.save(any())).thenReturn(enrollment)
            val response = enrollmentService.create(request)
            assertEquals(1L, response.id)
            assertTrue(response.active)
            verify(enrollmentRepository, times(1)).findByStudentIdAndCourseId(3L, 2L)
            verify(enrollmentRepository).save(any())
        }

        @Test
        fun `should throw exception when enrollment exists and already active`() {
            val enrollment = generateEnrollment()
            val request = CreateEnrollmentRequest(
                studentId = 3L,
                courseId = 2L,
            )
            whenever(enrollmentRepository.findByStudentIdAndCourseId(3L, 2L)).thenReturn(enrollment)
            assertThrows<EnrollmentAlreadyExistsException> {
                enrollmentService.create(request)
            }
            verify(enrollmentRepository, times(1)).findByStudentIdAndCourseId(3L, 2L)
            verify(enrollmentRepository, never()).save(any())
        }

        @Test
        fun `should throw exception when student not found`() {
            val request = CreateEnrollmentRequest(
                studentId = 3L,
                courseId = 2L,
            )
            whenever(enrollmentRepository.findByStudentIdAndCourseId(3L, 2L)).thenReturn(null)
            whenever(studentRepository.findById(3L)).thenReturn(Optional.empty<Student>())
            assertThrows<StudentNotFoundException> {
                enrollmentService.create(request)
            }
            verify(enrollmentRepository).findByStudentIdAndCourseId(3L, 2L)
            verify(studentRepository).findById(3L)
            verify(courseRepository, never()).findById(any())
            verify(enrollmentRepository, never()).save(any())
        }

        @Test
        fun `should throw exception when course not found`() {
            val request = CreateEnrollmentRequest(
                studentId = 3L,
                courseId = 2L,
            )
            val student = generateStudent()
            whenever(enrollmentRepository.findByStudentIdAndCourseId(3L, 2L)).thenReturn(null)
            whenever(studentRepository.findById(3L)).thenReturn(Optional.of(student))
            whenever(courseRepository.findById(2L)).thenReturn(Optional.empty<Course>())
            assertThrows<CourseNotFoundException> {
                enrollmentService.create(request)
            }
            verify(enrollmentRepository).findByStudentIdAndCourseId(3L, 2L)
            verify(studentRepository).findById(3L)
            verify(courseRepository).findById(2L)
            verify(enrollmentRepository, never()).save(any())
        }

    }

    @Nested
    inner class FindCoursesByStudent {
        @Test
        fun `should return enrollments by StudentId`() {
            val student = generateStudent()
            val enrollment = generateEnrollment()
            whenever(studentRepository.findById(3L)).thenReturn(Optional.of(student))
            whenever(enrollmentRepository.findByStudentIdAndActiveTrue(3L)).thenReturn(listOf(enrollment))
            val response = enrollmentService.findCoursesByStudent(3L)
            assertEquals(1, response.size)
            assertEquals(2L, response.first().id)
            assertEquals("Test Course", response.first().name)
            verify(enrollmentRepository).findByStudentIdAndActiveTrue(any<Long>())
            verify(studentRepository).findById(3L)
        }

        @Test
        fun `should throw exception when student not found`() {
            whenever(studentRepository.findById(3L)).thenReturn(Optional.empty())
            assertThrows<StudentNotFoundException> {
                enrollmentService.findCoursesByStudent(3L)
            }
            verify(enrollmentRepository, never()).findByStudentIdAndActiveTrue(any<Long>())
            verify(studentRepository).findById(3L)
        }
    }

    @Nested
    inner class FindStudentsByCourse {
        @Test
        fun `should return enrollments by CourseId`() {
            val course = generateCourse()
            val enrollment = generateEnrollment()
            whenever(courseRepository.findById(3L)).thenReturn(Optional.of(course))
            whenever(enrollmentRepository.findByCourseIdAndActiveTrue(3L)).thenReturn(listOf(enrollment))
            val response = enrollmentService.findStudentsByCourse(3L)
            assertEquals(1, response.size)
            assertEquals(3L, response.first().id)
            assertEquals("Test Student", response.first().name)
            verify(enrollmentRepository).findByCourseIdAndActiveTrue(any<Long>())
            verify(courseRepository).findById(3L)
        }

        @Test
        fun `should throw exception when course not found`() {
            whenever(courseRepository.findById(3L)).thenReturn(Optional.empty())
            assertThrows<CourseNotFoundException> {
                enrollmentService.findStudentsByCourse(3L)
            }
            verify(enrollmentRepository, never()).findByCourseIdAndActiveTrue(any<Long>())
            verify(courseRepository).findById(3L)
        }
    }
}
