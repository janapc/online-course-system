package com.janapc.online_course_system.enrollment.service

import com.janapc.online_course_system.course.dto.CourseSummaryResponse
import com.janapc.online_course_system.course.exception.CourseNotFoundException
import com.janapc.online_course_system.course.mapper.CourseMapper
import com.janapc.online_course_system.course.repository.CourseRepository
import com.janapc.online_course_system.enrollment.dto.CreateEnrollmentRequest
import com.janapc.online_course_system.enrollment.dto.EnrollmentDetailsResponse
import com.janapc.online_course_system.enrollment.dto.EnrollmentResponse
import com.janapc.online_course_system.enrollment.entity.Enrollment
import com.janapc.online_course_system.enrollment.exception.EnrollmentAlreadyCancelledException
import com.janapc.online_course_system.enrollment.exception.EnrollmentAlreadyExistsException
import com.janapc.online_course_system.enrollment.exception.EnrollmentNotFoundException
import com.janapc.online_course_system.enrollment.mapper.EnrollmentMapper
import com.janapc.online_course_system.enrollment.repository.EnrollmentRepository
import com.janapc.online_course_system.student.dto.StudentSummaryResponse
import com.janapc.online_course_system.student.exception.StudentNotFoundException
import com.janapc.online_course_system.student.mapper.StudentMapper
import com.janapc.online_course_system.student.repository.StudentRepository
import java.time.LocalDateTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class EnrollmentService(
    private val enrollmentRepository: EnrollmentRepository,
    private val studentRepository: StudentRepository,
    private val courseRepository: CourseRepository,
) {
    fun findAll(pageable: Pageable): Page<EnrollmentDetailsResponse> {
        return enrollmentRepository.findAll(pageable)
            .map { enrollment -> EnrollmentMapper.toDetailsResponse(enrollment) }
    }

    fun findById(id: Long): EnrollmentDetailsResponse {
        val enrollment = enrollmentRepository.findById(id).orElseThrow { EnrollmentNotFoundException(id) }
        return EnrollmentMapper.toDetailsResponse(enrollment)
    }

    fun create(request: CreateEnrollmentRequest): EnrollmentResponse {
        val enrollment =
            enrollmentRepository.findByStudentIdAndCourseId(
                request.studentId!!,
                request.courseId!!,
            )

        if (enrollment != null) {
            if (enrollment.active) {
                throw EnrollmentAlreadyExistsException()
            }
            enrollment.active = true
            enrollment.enrolledAt = LocalDateTime.now()
            return EnrollmentMapper.toResponse(
                enrollmentRepository
                    .save(enrollment),
            )
        }
        val student =
            studentRepository.findById(request.studentId)
                .orElseThrow {
                    StudentNotFoundException(request.studentId)
                }

        val course =
            courseRepository.findById(request.courseId)
                .orElseThrow {
                    CourseNotFoundException(request.courseId)
                }

        val newEnrollment =
            Enrollment(
                student = student,
                course = course,
            )
        return EnrollmentMapper.toResponse(
            enrollmentRepository
                .save(newEnrollment),
        )
    }

    fun findCoursesByStudent(studentId: Long): List<CourseSummaryResponse> {
        studentRepository.findById(studentId).orElseThrow { StudentNotFoundException(studentId) }
        return enrollmentRepository.findByStudentIdAndActiveTrue(studentId).map { enrollment ->
            CourseMapper.toSummaryResponse(enrollment.course)
        }
    }

    fun findStudentsByCourse(courseId: Long): List<StudentSummaryResponse> {
        courseRepository.findById(courseId).orElseThrow { CourseNotFoundException(courseId) }
        return enrollmentRepository.findByCourseIdAndActiveTrue(courseId).map { enrollment ->
            StudentMapper.toSummaryResponse(enrollment.student)
        }
    }

    fun cancel(id: Long): EnrollmentResponse {
        val enrollment = enrollmentRepository.findById(id).orElseThrow { EnrollmentNotFoundException(id) }
        if (!enrollment.active) {
            throw EnrollmentAlreadyCancelledException(id)
        }
        enrollment.active = false
        return EnrollmentMapper.toResponse(
            enrollmentRepository.save(enrollment),
        )
    }
}
