package com.janapc.online_course_system.student.service

import com.janapc.online_course_system.student.dto.CreateStudentRequest
import com.janapc.online_course_system.student.dto.UpdateStudentRequest
import com.janapc.online_course_system.student.entity.Student
import com.janapc.online_course_system.student.exception.StudentAlreadyExistsException
import com.janapc.online_course_system.student.exception.StudentNotFoundException
import com.janapc.online_course_system.student.repository.StudentRepository
import java.util.*
import kotlin.test.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification

@ExtendWith(MockitoExtension::class)
class StudentServiceTest {
    @Mock
    lateinit var studentRepository: StudentRepository

    @InjectMocks
    lateinit var studentService: StudentService

    @Nested
    inner class FindById {
        @Test
        fun `should return student when id exists`() {
            val student = Student(
                id = 1L,
                name = "John",
                email = "john@email.com",
                active = true,
            )
            whenever(studentRepository.findById(1L)).thenReturn(Optional.of(student))
            val response = studentService.findById(1L)
            assertEquals(1L, response.id)
            assertEquals("John", response.name)
            assertEquals("john@email.com", response.email)
            assertEquals(true, response.active)
            verify(studentRepository).findById(1L)
        }

        @Test
        fun `should throw exception when id does not exists`() {
            val id = 1L
            whenever(studentRepository.findById(id)).thenReturn(Optional.empty())
            val exception = assertThrows<StudentNotFoundException> { studentService.findById(id) }
            val expected = "Student with id $id not found"
            assertEquals(expected, exception.message)
            verify(studentRepository).findById(id)
        }
    }

    @Nested
    inner class CreateStudent {
        @Test
        fun `should create a student successfully`() {
            val request = CreateStudentRequest(
                name = "John",
                email = "john@email.com",
            )
            val savedStudent = Student(
                id = 1L,
                name = "John",
                email = "john@email.com",
                active = true,
            )
            whenever(studentRepository.existsByEmail(request.email)).thenReturn(false)
            whenever(studentRepository.save(any())).thenReturn(savedStudent)
            val response = studentService.create(request)
            assertEquals(1L, response.id)
            assertEquals("John", response.name)
            assertEquals("john@email.com", response.email)
            assertEquals(true, response.active)
            verify(studentRepository).existsByEmail(request.email)
            verify(studentRepository).save(any())
        }

        @Test
        fun `should throw exception when student already exists`() {
            val request = CreateStudentRequest(
                name = "John",
                email = "john@email.com",
            )
            whenever(studentRepository.existsByEmail(request.email)).thenReturn(true)
            val exception = assertThrows<StudentAlreadyExistsException> {
                studentService.create(request)
            }
            verify(studentRepository).existsByEmail(request.email)
            verify(studentRepository, never()).save(any())
            val expected = "Student with this email already exists"
            assertEquals(expected, exception.message)
        }
    }

    @Nested
    inner class DeleteStudent {
        @Test
        fun `should delete a student successfully`() {
            val student = Student(
                id = 1L,
                name = "John",
                email = "john@email.com",
                active = true,
            )
            val id = 1L
            whenever(studentRepository.findById(id)).thenReturn(Optional.of(student))
            studentService.delete(id)
            verify(studentRepository).findById(id)
            verify(studentRepository).delete(student)
        }

        @Test
        fun `should throw exception when try to delete a new student`() {
            val id = 1L
            whenever(studentRepository.findById(id)).thenReturn(Optional.empty())
            val exception = assertThrows<StudentNotFoundException> { studentService.delete(id) }
            verify(studentRepository).findById(id)
            verify(studentRepository, never()).delete(any<Student>())
            val expected = "Student with id $id not found"
            assertEquals(expected, exception.message)
        }
    }

    @Nested
    inner class UpdateStudent {
        @Test
        fun `should update a student successfully`() {
            val student = Student(
                id = 1L,
                name = "Maria",
                email = "maria@email.com",
                active = false,
            )
            val id = 1L
            whenever(studentRepository.findByEmail("maria@email.com")).thenReturn(null)
            whenever(studentRepository.findById(id)).thenReturn(Optional.of<Student>(student))
            val request = UpdateStudentRequest(
                name = "Maria",
                email = "maria@email.com",
                active = true,
            )

            whenever(studentRepository.save(student)).thenReturn(student)
            val response = studentService.update(id, request)
            assertEquals("Maria", response?.name)
            assertEquals("maria@email.com", response?.email)
            assertEquals(true, response?.active)

            assertEquals("Maria", student.name)
            assertEquals("maria@email.com", student.email)
            assertEquals(true, student.active)

            verify(studentRepository).findByEmail(request.email)
            verify(studentRepository).findById(1L)
            verify(studentRepository).save(student)
        }

        @Test
        fun `should throw StudentAlreadyExistsException when email already exists`() {
            val student = Student(
                id = 1L,
                name = "Maria",
                email = "maria@email.com",
                active = false,
            )
            val id = 2L
            whenever(studentRepository.findByEmail("maria@email.com")).thenReturn(student)
            val request = UpdateStudentRequest(
                name = "Maria",
                email = "maria@email.com",
                active = true,
            )

            val exception = assertThrows<StudentAlreadyExistsException> {
                studentService.update(id, request)
            }
            val expected = "Student with this email already exists"
            verify(studentRepository).findByEmail(request.email)
            verify(studentRepository, never()).findById(id)
            verify(studentRepository, never()).save(student)
            assertEquals(expected, exception.message)
        }

        @Test
        fun `should throw StudentNotFoundException when student does not exist`() {
            val student = Student(
                id = 1L,
                name = "Maria",
                email = "maria@email.com",
                active = false,
            )
            val id = 1L
            whenever(studentRepository.findByEmail("maria@email.com")).thenReturn(null)
            whenever(studentRepository.findById(id)).thenReturn(Optional.empty())
            val request = UpdateStudentRequest(
                name = "Maria",
                email = "maria@email.com",
                active = true,
            )

            val exception = assertThrows<StudentNotFoundException> {
                studentService.update(id, request)
            }
            val expected = "Student with id $id not found"
            verify(studentRepository).findByEmail(request.email)
            verify(studentRepository).findById(id)
            verify(studentRepository, never()).save(student)
            assertEquals(expected, exception.message)
        }
    }

    @Nested
    inner class FindAll {
        @Test
        fun `should return students page`() {
            val student = Student(
                id = 1L,
                name = "Maria",
                email = "maria@email.com",
                active = true,
            )
            val page = PageImpl(listOf(student))
            val pageable = PageRequest.of(0, 10)
            whenever(
                studentRepository.findAll(
                    any<Specification<Student>>(),
                    eq(pageable),
                ),
            ).thenReturn(
                page,
            )
            val response = studentService.findAll(
                null,
                null,
                pageable,
            )
            assertEquals(1, response.totalElements)
            assertEquals(1, response.totalPages)
            assertEquals("Maria", response.content.first().name)
            assertEquals("maria@email.com", response.content.first().email)
            verify(studentRepository).findAll(
                any<Specification<Student>>(),
                eq(pageable),
            )
        }
    }

    @Nested
    inner class CreateAllInBatch {
        @Test
        @DisplayName("Shoul create all students")
        fun shouldCreateAllStudents() {
            val students = listOf(
                Student(
                    id = 1L,
                    name = "Student 1",
                    email = "student1@email.com",
                    active = true,
                ),
                Student(
                    id = 2L,
                    name = "Student 2",
                    email = "student2@email.com",
                    active = true,
                ),
            )
            val request = listOf(
                CreateStudentRequest(
                    name = "Student 1",
                    email = "student1@email.com",
                ),
                CreateStudentRequest(
                    name = "Student 2",
                    email = "student2@email.com",
                ),
            )
            whenever(studentRepository.saveAll(any<Iterable<Student>>())).thenReturn(students)
            studentService.createAllInBatch(request)
            verify(studentRepository).saveAll(any<Iterable<Student>>())
        }
    }
}
