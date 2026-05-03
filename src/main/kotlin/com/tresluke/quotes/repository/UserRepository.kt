package com.tresluke.quotes.repository

import com.tresluke.quotes.document.User
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, String> {
    fun findByEmail(email: String): User?
}
