package io.opendmp.processor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OpendmpProcessorApplication

fun main(args: Array<String>) {
	runApplication<OpendmpProcessorApplication>(*args)
}
