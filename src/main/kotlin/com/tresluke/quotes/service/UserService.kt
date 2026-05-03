package com.tresluke.quotes.service

import com.tresluke.quotes.document.User
import com.tresluke.quotes.dto.RegisterRequest
import com.tresluke.quotes.exception.EmailAlreadyExistsException
import com.tresluke.quotes.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
): UserDetailsService {

    fun register(request: RegisterRequest): User {
        if (userRepository.findByEmail(request.email) != null) {
            throw EmailAlreadyExistsException(request.email)
        }
        return userRepository.save(
            User(
                email = request.email,
                passwordHash = requireNotNull(passwordEncoder.encode(request.password)) { "Password encoding failed" }
            )
        )
    }

    override fun loadUserByUsername(email: String): UserDetails =
        userRepository.findByEmail(email)
            ?.let { user ->
                org.springframework.security.core.userdetails.User(
                    user.email,
                    user.passwordHash,
                    listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
                )
            }
            ?: throw UsernameNotFoundException("User not found: $email")
}
