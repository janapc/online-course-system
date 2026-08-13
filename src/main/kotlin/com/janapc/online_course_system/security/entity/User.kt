package com.janapc.online_course_system.security.entity

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    private val password: String,

    @Enumerated(EnumType.STRING)
    val role: Role = Role.USER,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority?>? = listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun getPassword(): String? = password
    override fun getUsername(): String? = email


}
