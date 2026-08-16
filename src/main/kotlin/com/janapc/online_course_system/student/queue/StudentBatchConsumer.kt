package com.janapc.online_course_system.student.queue

import com.janapc.online_course_system.common.config.RabbitMQConfig
import com.janapc.online_course_system.student.dto.CreateStudentRequest
import com.janapc.online_course_system.student.service.StudentService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class StudentBatchConsumer(
    private val studentService: StudentService,
) {
    private val logger = LoggerFactory.getLogger(StudentBatchConsumer::class.java)

    @RabbitListener(
        queues = [RabbitMQConfig.STUDENT_BATCH_QUEUE],
        containerFactory = "batchContainerFactory",
    )
    fun processBatch(students: List<CreateStudentRequest>) {
        logger.info("Received a batch of {} student messages from RabbitMQ", students.size)
        try {
            studentService.createAllInBatch(students)
            logger.info("Successfully processed and saved batch of {} students to the database", students.size)
        } catch (ex: Exception) {
            logger.error("Failed to process student creation batch: {}", ex.message, ex)
            throw ex
        }
    }
}
