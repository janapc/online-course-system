package com.janapc.online_course_system.student.specification

import com.janapc.online_course_system.student.entity.Student
import org.springframework.data.jpa.domain.Specification

object StudentSpecification {
    fun hasName(name: String): Specification<Student> =
        Specification { root, _, cb ->
            cb.like(
                cb.lower(root.get("name")),
                "%${name.lowercase()}%",
            )
        }

    fun hasActive(active: Boolean): Specification<Student> =
        Specification { root, _, cb ->
            cb.equal(root.get<Boolean>("active"), active)
        }

}
