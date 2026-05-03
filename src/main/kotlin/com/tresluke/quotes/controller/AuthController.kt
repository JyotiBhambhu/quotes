package com.tresluke.quotes.controller

import com.tresluke.quotes.dto.AuthResponse
import com.tresluke.quotes.dto.LoginRequest
import com.tresluke.quotes.dto.RegisterRequest
import com.tresluke.quotes.service.JwtService
import com.tresluke.quotes.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: RegisterRequest): AuthResponse {
        userService.register(request)
        return AuthResponse(jwtService.generateToken(request.email))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )
        return AuthResponse(jwtService.generateToken(request.email))
    }
}
