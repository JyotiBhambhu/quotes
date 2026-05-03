package com.tresluke.quotes.exception

class EmailAlreadyExistsException(email: String) : RuntimeException("Email already registered: $email")
