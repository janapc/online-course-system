package com.janapc.online_course_system.student.service

import com.janapc.online_course_system.student.dto.CreateStudentBatchRequest
import com.janapc.online_course_system.student.dto.CreateStudentRequest
import com.janapc.online_course_system.student.dto.StudentResponse
import com.janapc.online_course_system.student.dto.UpdateStudentRequest
import com.janapc.online_course_system.student.entity.Student
import com.janapc.online_course_system.student.exception.StudentAlreadyExistsException
import com.janapc.online_course_system.student.exception.StudentNotFoundException
import com.janapc.online_course_system.student.mapper.StudentMapper
import com.janapc.online_course_system.student.queue.StudentBatchProducer
import com.janapc.online_course_system.student.repository.StudentRepository
import com.janapc.online_course_system.student.specification.StudentSpecification
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudentService(
    private val studentRepository: StudentRepository,
    private val studentBatchProducer: StudentBatchProducer,
) {

    fun findAll(name: String?, active: Boolean?, pageable: Pageable): Page<StudentResponse> {
        var specification: Specification<Student> = Specification.allOf()

        if (!name.isNullOrBlank()) {
            specification = specification.and(
                StudentSpecification.hasName(name),
            )
        }
        if (active != null) {
            specification = specification.and(
                StudentSpecification.hasActive(active),
            )
        }
        return studentRepository.findAll(specification, pageable).map { StudentMapper.toResponse(it) }
    }

    @Transactional
    @CacheEvict(value = ["students"], allEntries = true)
    fun create(student: CreateStudentRequest): StudentResponse {
        val student = Student(
            name = student.name,
            email = student.email,
            active = true,
        )
        if (studentRepository.existsByEmail(student.email)) {
            throw StudentAlreadyExistsException()
        }
        val savedStudent = studentRepository.save(student)
        return StudentMapper.toResponse(savedStudent)
    }

    @Cacheable(value = ["students"], key = "#id")
    fun findById(id: Long): StudentResponse {
        val student = studentRepository.findById(id).orElseThrow {
            StudentNotFoundException(id)
        }
        return StudentMapper.toResponse(student)
    }

    @Transactional
    @CacheEvict(value = ["students"], allEntries = true)
    fun delete(id: Long) {
        val student = studentRepository.findById(id).orElseThrow { StudentNotFoundException(id) }
        studentRepository.delete(student)
    }

    @Transactional
    @CacheEvict(value = ["students"], allEntries = true)
    fun update(id: Long, newStudent: UpdateStudentRequest): StudentResponse? {
        val studentWithEmail = studentRepository.findByEmail(newStudent.email)
        if (studentWithEmail != null && studentWithEmail.id != id) {
            throw StudentAlreadyExistsException()
        }
        val student = studentRepository.findById(id)
            .orElseThrow {
                StudentNotFoundException(id)
            }
        student.name = newStudent.name
        student.email = newStudent.email
        student.active = newStudent.active
        return StudentMapper.toResponse(
            studentRepository.save(student),
        )
    }

    @Transactional
    fun createAllBatchStudents(students: List<CreateStudentRequest>) {
        val entities = students.map { dto ->
            Student(
                name = dto.name,
                email = dto.email,
                active = true,
            )
        }
        studentRepository.saveAll(entities)
    }

    fun sendStudentsToQueue(request: CreateStudentBatchRequest) {
        studentBatchProducer.sendToQueue(students = request.students)
    }
}
