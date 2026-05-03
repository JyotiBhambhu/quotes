package com.tresluke.quotes.repository

import com.tresluke.quotes.document.Quote
import org.springframework.data.mongodb.repository.MongoRepository

interface QuoteRepository : MongoRepository<Quote, String>
