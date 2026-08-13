package com.janapc.online_course_system

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test") // 👈 Adicione esta anotação aqui!
class OnlineCourseSystemApplicationTests {

    @Test
    fun contextLoads() {
    }
}
