package com.tresluke.quotes.service

import com.tresluke.quotes.document.Quote
import com.tresluke.quotes.exception.QuoteNotFoundException
import com.tresluke.quotes.repository.QuoteRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicReference

@Service
class DailyQuoteService(private val quoteRepository: QuoteRepository) {

    private val todaysQuote = AtomicReference<Quote?>(null)

    @Scheduled(cron = "0 0 0 * * *")
    fun rotateDailyQuote() {
        todaysQuote.set(pickRandom())
    }

    fun getToday(): Quote {
        return todaysQuote.get() ?: pickRandom().also { todaysQuote.set(it) }
    }

    private fun pickRandom(): Quote {
        val all = quoteRepository.findAll()
        if (all.isEmpty()) throw QuoteNotFoundException("no quotes exist yet")
        return all.random()
    }
}
