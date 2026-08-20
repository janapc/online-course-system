package com.janapc.online_course_system.student.queue

import com.janapc.online_course_system.student.dto.CreateStudentRequest
import com.janapc.online_course_system.student.service.StudentService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class StudentBatchConsumerTest {
	@Mock
	private lateinit var studentService: StudentService

	@InjectMocks
	private lateinit var studentBatchConsumer: StudentBatchConsumer

	@Test
	@DisplayName("Should call studentService createAllBatchStudents when batch is received from queue")
	fun shouldProcessStudentBatchSuccessfully() {
		val students =
			listOf(
				CreateStudentRequest(name = "Student 1", email = "student1@email.com"),
				CreateStudentRequest(name = "Student 2", email = "student2@email.com"),
			)
		studentBatchConsumer.processBatch(students)
		verify(studentService).createAllBatchStudents(students)
	}
}
