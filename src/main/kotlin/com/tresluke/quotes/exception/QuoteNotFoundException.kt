package com.tresluke.quotes.exception

class QuoteNotFoundException(id: String) : RuntimeException("Quote not found: $id")
