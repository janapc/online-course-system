package com.janapc.online_course_system.common.config

import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {
	companion object {
		const val STUDENT_BATCH_QUEUE = "student.creation.batch.queue"
		const val COURSE_BATCH_QUEUE = "course.creation.batch.queue"
	}

	/**
	 * declara a fila no rabbitmq
	 */
	@Bean
	fun studentBatchQueue(): Queue = Queue(STUDENT_BATCH_QUEUE, true)

	@Bean
	fun courseBatchQueue(): Queue = Queue(COURSE_BATCH_QUEUE, true)

	/**
	 * converte objetos kotlin/java para json e envia pra fila e converte json de volta para objeto ao ler da fila
	 */
	@Bean
	fun jsonMessageConverter(): Jackson2JsonMessageConverter = Jackson2JsonMessageConverter()

	/**
	 * configura o comportamento de leitura em batch
	 */
	@Bean
	fun batchContainerFactory(connectionFactory: ConnectionFactory): SimpleRabbitListenerContainerFactory {
		val factory = SimpleRabbitListenerContainerFactory()
		factory.setConnectionFactory(connectionFactory)
		factory.setMessageConverter(jsonMessageConverter())
		factory.setBatchListener(true)
		factory.setBatchSize(50)
		factory.setReceiveTimeout(300L)
		return factory
	}
}
