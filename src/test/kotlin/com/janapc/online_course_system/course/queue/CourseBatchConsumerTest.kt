package com.janapc.online_course_system.course.queue

import com.janapc.online_course_system.course.dto.CreateCourseRequest
import com.janapc.online_course_system.course.service.CourseService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class CourseBatchConsumerTest {
	@Mock
	private lateinit var courseService: CourseService

	@InjectMocks
	private lateinit var courseBatchConsumer: CourseBatchConsumer

	@Test
	@DisplayName("Should call CourseService createAllBatchCourses when batch is received from queue")
	fun shouldProcessCourseBatchSuccessfully() {
		val courses =
			listOf(
				CreateCourseRequest(name = "Course A", description = "This is a description A"),
				CreateCourseRequest(name = "Course B", description = "This is a description B"),
			)
		courseBatchConsumer.processBatch(courses)
		verify(courseService).createAllBatchCourses(courses)
	}
}
