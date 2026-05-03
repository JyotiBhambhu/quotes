package com.tresluke.quotes.controller

import com.tresluke.quotes.dto.QuoteRequest
import com.tresluke.quotes.dto.QuoteResponse
import com.tresluke.quotes.service.QuoteService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/quotes")
class QuoteController(private val quoteService: QuoteService) {

    @GetMapping
    fun getAll(pageable: Pageable): Page<QuoteResponse> =
        quoteService.getAll(pageable).map { QuoteResponse.from(it) }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): QuoteResponse =
        QuoteResponse.from(quoteService.getById(id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: QuoteRequest): QuoteResponse =
        QuoteResponse.from(quoteService.create(request))

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @Valid @RequestBody request: QuoteRequest): QuoteResponse =
        QuoteResponse.from(quoteService.update(id, request))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: String) = quoteService.delete(id)

    @PostMapping("/{id}/like")
    fun like(@PathVariable id: String): QuoteResponse =
        QuoteResponse.from(quoteService.like(id))
}
