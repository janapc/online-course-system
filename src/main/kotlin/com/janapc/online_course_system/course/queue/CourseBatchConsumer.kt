package com.janapc.online_course_system.course.queue

import com.janapc.online_course_system.common.config.RabbitMQConfig
import com.janapc.online_course_system.course.dto.CreateCourseRequest
import com.janapc.online_course_system.course.service.CourseService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class CourseBatchConsumer(
    private val courseService: CourseService,
) {
    private val logger = LoggerFactory.getLogger(CourseBatchConsumer::class.java)

    @RabbitListener(
        queues = [RabbitMQConfig.COURSE_BATCH_QUEUE],
        containerFactory = "batchContainerFactory",
    )
    fun processBatch(courses: List<CreateCourseRequest>) {
        logger.info("Processing courses {}", courses.size)
        try {
            courseService.createAllBatchCourses(courses)
            logger.info("Successfully processed courses {}", courses.size)
        } catch (ex: Exception) {
            logger.error("Error processing courses", ex)
            throw ex
        }
    }
}
