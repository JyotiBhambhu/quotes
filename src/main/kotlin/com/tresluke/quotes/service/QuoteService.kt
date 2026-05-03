package com.tresluke.quotes.service

import com.tresluke.quotes.document.Quote
import com.tresluke.quotes.dto.QuoteRequest
import com.tresluke.quotes.exception.QuoteNotFoundException
import com.tresluke.quotes.repository.QuoteRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class QuoteService(private val quoteRepository: QuoteRepository) {

    fun getAll(pageable: Pageable): Page<Quote> = quoteRepository.findAll(pageable)

    fun getById(id: String): Quote = quoteRepository.findById(id)
        .orElseThrow { QuoteNotFoundException(id) }

    fun create(request: QuoteRequest): Quote =
        quoteRepository.save(Quote(text = request.text, author = request.author))

    fun update(id: String, request: QuoteRequest): Quote {
        val existing = getById(id)
        return quoteRepository.save(existing.copy(text = request.text, author = request.author))
    }

    fun delete(id: String) {
        if (!quoteRepository.existsById(id)) throw QuoteNotFoundException(id)
        quoteRepository.deleteById(id)
    }

    fun like(id: String): Quote {
        val existing = getById(id)
        return quoteRepository.save(existing.copy(likes = existing.likes + 1))
    }
}
