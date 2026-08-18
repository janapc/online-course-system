package com.janapc.online_course_system.course.queue

import com.janapc.online_course_system.common.config.RabbitMQConfig.Companion.COURSE_BATCH_QUEUE
import com.janapc.online_course_system.course.dto.CreateCourseRequest
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class CourseBatchProducer(
    private val rabbitTemplate: RabbitTemplate,
) {
    private val logger = LoggerFactory.getLogger(CourseBatchProducer::class.java)

    fun sendToQueue(courses: List<CreateCourseRequest>) {
        logger.info("Sending {} course(s)", courses.size)
        courses.forEach { course ->
            logger.debug("Publishing course with name {}", course.name)
            rabbitTemplate.convertAndSend(COURSE_BATCH_QUEUE, course)
        }
        logger.info("Successfully published {} course(s) messages to queue {}", courses.size, COURSE_BATCH_QUEUE)
    }
}
