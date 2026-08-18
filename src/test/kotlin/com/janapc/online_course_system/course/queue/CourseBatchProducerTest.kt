package com.janapc.online_course_system.course.queue

import com.janapc.online_course_system.course.dto.CreateCourseRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.amqp.rabbit.core.RabbitTemplate

@ExtendWith(MockitoExtension::class)
class CourseBatchProducerTest {

    @Mock
    private lateinit var rabbitTemplate: RabbitTemplate

    @InjectMocks
    private lateinit var courseBatchProducer: CourseBatchProducer

    @Test
    @DisplayName("Should send a course to the queue")
    fun shouldSendACourseToQueueSuccessfully() {
        doNothing().whenever(rabbitTemplate)
            .convertAndSend(
                any<String>(),
                any<CreateCourseRequest>(),
            )
        val courses = listOf(
            CreateCourseRequest(name = "Course A", description = "This is a description A"),
            CreateCourseRequest(name = "Course B", description = "This is a description B"),
        )
        courseBatchProducer.sendToQueue(courses)
        verify(rabbitTemplate, times(2)).convertAndSend(
            eq("course.creation.batch.queue"),
            any<CreateCourseRequest>(),
        )
    }
}
