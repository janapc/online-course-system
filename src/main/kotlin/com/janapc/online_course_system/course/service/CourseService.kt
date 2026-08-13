package com.janapc.online_course_system.course.service

import com.janapc.online_course_system.course.dto.CourseResponse
import com.janapc.online_course_system.course.dto.CreateCourseRequest
import com.janapc.online_course_system.course.dto.UpdateCourseRequest
import com.janapc.online_course_system.course.entity.Course
import com.janapc.online_course_system.course.exception.CourseNotFoundException
import com.janapc.online_course_system.course.mapper.CourseMapper
import com.janapc.online_course_system.course.repository.CourseRepository
import com.janapc.online_course_system.course.specification.CourseSpecification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service

@Service
class CourseService(
    private val courseRepository: CourseRepository,
) {
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

    fun findById(id: Long): CourseResponse {
        val course = courseRepository.findById(id).orElseThrow {
            CourseNotFoundException(id)
        }
        return CourseMapper.toResponse(course)
    }

    fun update(id: Long, request: UpdateCourseRequest): CourseResponse {
        val course = courseRepository.findById(id).orElseThrow { CourseNotFoundException(id) }
        course.name = request.name
        course.description = request.description
        course.active = request.active
        return CourseMapper.toResponse(
            courseRepository.save(course),
        )
    }

    fun delete(id: Long) {
        val course = courseRepository.findById(id).orElseThrow { CourseNotFoundException(id) }
        courseRepository.delete(course)
    }
}
