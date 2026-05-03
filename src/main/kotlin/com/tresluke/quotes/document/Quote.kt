package com.tresluke.quotes.document

import jakarta.validation.constraints.NotBlank
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "quotes")
data class Quote(
    @Id val id: String? = null,
    @NotBlank val text: String,
    @NotBlank val author: String,
    val createdAt: Instant = Instant.now(),
    val likes: Int = 0
)
