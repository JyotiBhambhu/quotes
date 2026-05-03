package com.tresluke.quotes.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService {

    @Value("\${jwt.secret}")
    private lateinit var secret: String

    @Value("\${jwt.expiration-ms}")
    private var expirationMs: Long = 0

    fun generateToken(email: String): String =
        Jwts.builder()
            .subject(email)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(signingKey())
            .compact()

    fun extractEmail(token: String): String =
        parseClaims(token).subject

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean =
        extractEmail(token) == userDetails.username && !isTokenExpired(token)

    private fun isTokenExpired(token: String): Boolean =
        parseClaims(token).expiration.before(Date())

    private fun parseClaims(token: String) =
        Jwts.parser()
            .verifyWith(signingKey())
            .build()
            .parseSignedClaims(token)
            .payload

    private fun signingKey(): SecretKey =
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
}
