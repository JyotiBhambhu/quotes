package com.tresluke.quotes.dto

import com.tresluke.quotes.document.Quote
import java.time.Instant

data class QuoteResponse(
    val id: String,
    val text: String,
    val author: String,
    val createdAt: Instant,
    val likes: Int
) {
    companion object {
        fun from(quote: Quote) = QuoteResponse(
            id = requireNotNull(quote.id) { "Quote must have an id" },
            text = quote.text,
            author = quote.author,
            createdAt = quote.createdAt,
            likes = quote.likes
        )
    }
}
