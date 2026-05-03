package com.tresluke.quotes.service

import com.tresluke.quotes.document.Quote
import com.tresluke.quotes.exception.QuoteNotFoundException
import com.tresluke.quotes.repository.QuoteRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicReference

@Service
class DailyQuoteService(private val quoteRepository: QuoteRepository) {

    private val log = LoggerFactory.getLogger(DailyQuoteService::class.java)
    private val todaysQuote = AtomicReference<Quote?>(null)

    @Scheduled(cron = "0 * * * * *")  // every minute — change back to "0 0 0 * * *" after testing
    fun rotateDailyQuote() {
        val quote = pickRandom()
        todaysQuote.set(quote)
        log.info("Daily quote rotated: [${quote.id}] ${quote.author}")
    }

    fun getToday(): Quote =
        todaysQuote.get() ?: pickRandom().also { todaysQuote.set(it) }

    private fun pickRandom(): Quote {
        val all = quoteRepository.findAll()
        if (all.isEmpty()) throw QuoteNotFoundException("no quotes exist yet — add some first")
        return all.random()
    }
}
