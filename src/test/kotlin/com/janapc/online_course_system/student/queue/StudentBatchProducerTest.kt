package com.janapc.online_course_system.student.queue

import com.janapc.online_course_system.student.dto.CreateStudentRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.amqp.rabbit.core.RabbitTemplate

@ExtendWith(MockitoExtension::class)
class StudentBatchProducerTest {

    @Mock
    private lateinit var rabbitTemplate: RabbitTemplate

    @InjectMocks
    private lateinit var studentBatchProducer: StudentBatchProducer

    @Test
    @DisplayName("Should send a student to the queue")
    fun shouldSendAStudentToQueueSuccessfully() {
        doNothing().whenever(rabbitTemplate)
            .convertAndSend(
                any<String>(),
                any<CreateStudentRequest>(),
            )
        val students = listOf(
            CreateStudentRequest(name = "Test A", email = "test+a@email.com"),
            CreateStudentRequest(name = "Test A", email = "test+b@email.com"),
        )
        studentBatchProducer.sendToQueue(students)
        verify(rabbitTemplate, times(2)).convertAndSend(
            eq("student.creation.batch.queue"),
            any<CreateStudentRequest>(),
        )
    }
}
