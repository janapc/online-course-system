package com.janapc.online_course_system.course.specification

import com.janapc.online_course_system.course.entity.Course
import org.springframework.data.jpa.domain.Specification

object CourseSpecification {
	fun hasName(name: String): Specification<Course> =
		Specification { root, _, cb ->
			cb.like(
				cb.lower(root.get("name")),
				"%${name.lowercase()}%",
			)
		}

	fun hasActive(active: Boolean): Specification<Course> =
		Specification { root, _, cb ->
			cb.equal(root.get<Boolean>("active"), active)
		}
}
