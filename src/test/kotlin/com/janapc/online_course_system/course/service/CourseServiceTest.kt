package com.janapc.online_course_system.course.service

import com.janapc.online_course_system.course.dto.CreateCourseBatchRequest
import com.janapc.online_course_system.course.dto.CreateCourseRequest
import com.janapc.online_course_system.course.dto.UpdateCourseRequest
import com.janapc.online_course_system.course.entity.Course
import com.janapc.online_course_system.course.exception.CourseNotFoundException
import com.janapc.online_course_system.course.queue.CourseBatchProducer
import com.janapc.online_course_system.course.repository.CourseRepository
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.assertj.core.api.Assertions.assertThat
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
class CourseServiceTest {
	@Mock
	lateinit var courseRepository: CourseRepository

	@Mock
	lateinit var courseBatchProducer: CourseBatchProducer

	@InjectMocks
	lateinit var courseService: CourseService

	@Test
	fun `should create a new course`() {
		val course =
			Course(
				id = 1,
				name = "Course A",
				description = "This is a description",
				active = true,
			)
		whenever(courseRepository.save(any())).thenReturn(course)
		val request =
			CreateCourseRequest(
				name = "Course A",
				description = "This is a description",
			)
		val response = courseService.create(request)
		assertThat(response).isNotNull
		assertEquals(1, response.id)
		assertEquals("Course A", response.name)
		assertEquals("This is a description", response.description)
		assertTrue(response.active)
		verify(courseRepository).save(any())
	}

	@Test
	fun `should return list of courses`() {
		val course =
			Course(
				id = 1L,
				name = "Course A",
				description = "This is a description",
				active = true,
			)
		val pageable = PageRequest.of(0, 10)
		val page = PageImpl<Course>(listOf(course))
		whenever(
			courseRepository.findAll(
				any<Specification<Course>>(),
				eq(pageable),
			),
		).thenReturn(page)
		val response = courseService.findAll(null, null, pageable)
		assertEquals(1, response.totalElements)
		assertEquals("Course A", response.content.first().name)
		assertEquals("This is a description", response.content.first().description)
		verify(courseRepository).findAll(
			any<Specification<Course>>(),
			eq(pageable),
		)
	}

	@Test
	fun `should return a course`() {
		val course =
			Course(
				id = 1L,
				name = "Course A",
				description = "This is a description",
				active = true,
			)
		whenever(courseRepository.findById(1L)).thenReturn(Optional.of(course))
		val response = courseService.findById(1L)
		assertEquals(1L, response.id)
		assertEquals("Course A", response.name)
		assertEquals("This is a description", response.description)
		assertTrue(response.active)
	}

	@Test
	fun `should throw exception when course by id not found`() {
		whenever(courseRepository.findById(1L)).thenReturn(Optional.empty())
		assertThrows<CourseNotFoundException> {
			courseService.findById(1L)
		}
		verify(courseRepository).findById(1L)
	}

	@Test
	fun `should update existing course`() {
		val course =
			Course(
				id = 1L,
				name = "Course A",
				description = "This is a description",
				active = false,
			)
		whenever(courseRepository.findById(1L)).thenReturn(Optional.of(course))
		whenever(courseRepository.save(any())).thenReturn(course)
		val request =
			UpdateCourseRequest(
				name = "Course A",
				description = "This is a description",
				active = true,
			)
		val response = courseService.update(1L, request)
		assertEquals(1L, response.id)
		assertEquals("Course A", response.name)
		assertEquals("This is a description", response.description)
		assertTrue(response.active)
		verify(courseRepository).findById(1L)
		verify(courseRepository).save(any())
	}

	@Test
	fun `should throw an exception when the course to be updated is not found`() {
		whenever(courseRepository.findById(1L)).thenReturn(Optional.empty())
		val request =
			UpdateCourseRequest(
				name = "Course A",
				description = "This is a description",
				active = true,
			)
		assertThrows<CourseNotFoundException> {
			courseService.update(1L, request)
		}
		verify(courseRepository).findById(1L)
		verify(courseRepository, never()).save(any())
	}

	@Test
	fun `should delete existing course`() {
		val course =
			Course(
				id = 1L,
				name = "Course A",
				description = "This is a description",
				active = false,
			)
		whenever(courseRepository.findById(1L)).thenReturn(Optional.of(course))
		courseService.delete(1L)
		verify(courseRepository).findById(1L)
		verify(courseRepository).delete(course)
	}

	@Test
	fun `should throw an exception when the course to be delete is not found`() {
		whenever(courseRepository.findById(1L)).thenReturn(Optional.empty())
		assertThrows<CourseNotFoundException> {
			courseService.delete(1L)
		}
		verify(courseRepository).findById(1L)
		verify(courseRepository, never()).delete(any<Course>())
	}

	@Test
	fun `should send course message to queue`() {
		val request =
			CreateCourseBatchRequest(
				listOf(
					CreateCourseRequest(
						name = "Course A",
						description = "This is a description A",
					),
					CreateCourseRequest(
						name = "Course B",
						description = "This is a description B",
					),
				),
			)
		doNothing().whenever(courseBatchProducer).sendToQueue(request.courses)
		courseService.sendCoursesToQueue(request)
		verify(courseBatchProducer).sendToQueue(any())
	}

	@Test
	fun `should create all batch courses`() {
		val courses =
			listOf(
				Course(id = 1L, name = "Course A", description = "This is a description A", active = true),
				Course(id = 2L, name = "Course B", description = "This is a description B", active = true),
			)
		val request =
			listOf(
				CreateCourseRequest(
					name = "Course A",
					description = "This is a description A",
				),
				CreateCourseRequest(
					name = "Course B",
					description = "This is a description B",
				),
			)
		whenever(courseRepository.saveAll(any<Iterable<Course>>())).thenReturn(courses)
		courseService.createAllBatchCourses(request)
		verify(courseRepository).saveAll(any<Iterable<Course>>())
	}
}
