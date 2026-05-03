package com.tresluke.quotes.dto

import jakarta.validation.constraints.NotBlank

data class QuoteRequest(
    @field:NotBlank val text: String,
    @field:NotBlank val author: String
)
