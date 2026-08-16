package com.janapc.online_course_system.student.queue

import com.janapc.online_course_system.common.config.RabbitMQConfig
import com.janapc.online_course_system.student.dto.CreateStudentRequest
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class StudentBatchProducer(
    private val rabbitTemplate: RabbitTemplate,
) {
    private val logger = LoggerFactory.getLogger(StudentBatchProducer::class.java)

    fun sendToQueue(students: List<CreateStudentRequest>) {
        logger.info("Enqueueing a batch of {} student creation requests", students.size)
        students.forEach { student ->
            logger.debug("Publishing student creation message for email: {}", student.email)
            rabbitTemplate.convertAndSend(RabbitMQConfig.STUDENT_BATCH_QUEUE, student)
        }
        logger.info(
            "Successfully published {} student messages to queue '{}'",
            students.size,
            RabbitMQConfig.STUDENT_BATCH_QUEUE,
        )
    }
}
