package com.tresluke.quotes

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class QuotesApplication

fun main(args: Array<String>) {
	runApplication<QuotesApplication>(*args)
}
