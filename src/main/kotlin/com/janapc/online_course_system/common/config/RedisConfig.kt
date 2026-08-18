package com.janapc.online_course_system.common.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import java.time.Duration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@EnableCaching
@Profile("!test")
class RedisConfig {

    @Bean
    fun redisCacheManager(
        connectionFactory: RedisConnectionFactory,
        objectMapper: ObjectMapper,
    ): RedisCacheManager {
        val redisObjectMapper = objectMapper.copy()

        val typeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Any::class.java)
            .build()

        redisObjectMapper.activateDefaultTyping(
            typeValidator,
            ObjectMapper.DefaultTyping.EVERYTHING,
            JsonTypeInfo.As.PROPERTY,
        )

        val serializer = GenericJackson2JsonRedisSerializer(redisObjectMapper)

        val cacheConfiguration = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()),
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(serializer),
            )

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(cacheConfiguration)
            .build()
    }
}
