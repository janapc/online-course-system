package com.janapc.online_course_system.course.service

import com.janapc.online_course_system.course.dto.CourseResponse
import com.janapc.online_course_system.course.dto.CreateCourseBatchRequest
import com.janapc.online_course_system.course.dto.CreateCourseRequest
import com.janapc.online_course_system.course.dto.UpdateCourseRequest
import com.janapc.online_course_system.course.entity.Course
import com.janapc.online_course_system.course.exception.CourseNotFoundException
import com.janapc.online_course_system.course.mapper.CourseMapper
import com.janapc.online_course_system.course.queue.CourseBatchProducer
import com.janapc.online_course_system.course.repository.CourseRepository
import com.janapc.online_course_system.course.specification.CourseSpecification
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val courseBatchProducer: CourseBatchProducer,
) {
    @Transactional
    @CacheEvict(value = ["courses"], allEntries = true)
    fun create(request: CreateCourseRequest): CourseResponse {
        val course = Course(name = request.name, description = request.description)
        return CourseMapper.toResponse(
            courseRepository.save(course),
        )
    }

    fun findAll(name: String?, active: Boolean?, pageable: Pageable): Page<CourseResponse> {
        var specification: Specification<Course> = Specification.allOf();
        if (!name.isNullOrBlank()) {
            specification = specification.and(CourseSpecification.hasName(name))
        }

        if (active != null) {
            specification = specification.and(CourseSpecification.hasActive(active))
        }

        return courseRepository.findAll(specification, pageable).map { CourseMapper.toResponse(it) }
    }

    @Cacheable(value = ["courses"], key = "#id")
    fun findById(id: Long): CourseResponse {
        val course = courseRepository.findById(id).orElseThrow {
            CourseNotFoundException(id)
        }
        return CourseMapper.toResponse(course)
    }

    @Transactional
    @CacheEvict(cacheNames = ["courses"], key = "#id")
    fun update(id: Long, request: UpdateCourseRequest): CourseResponse {
        val course = courseRepository.findById(id).orElseThrow { CourseNotFoundException(id) }
        course.name = request.name
        course.description = request.description
        course.active = request.active
        return CourseMapper.toResponse(
            courseRepository.save(course),
        )
    }

    @Transactional
    @CacheEvict(cacheNames = ["courses"], key = "#id")
    fun delete(id: Long) {
        val course = courseRepository.findById(id).orElseThrow { CourseNotFoundException(id) }
        courseRepository.delete(course)
    }

    @Transactional
    fun createAllBatchCourses(courses: List<CreateCourseRequest>) {
        val entities = courses.map { dto ->
            Course(
                name = dto.name,
                description = dto.description,
            )
        }
        courseRepository.saveAll(entities)
    }

    fun sendCoursesToQueue(request: CreateCourseBatchRequest) {
        courseBatchProducer.sendToQueue(courses = request.courses)
    }
}
