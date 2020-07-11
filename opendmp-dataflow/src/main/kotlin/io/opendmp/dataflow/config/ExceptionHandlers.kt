package io.opendmp.dataflow.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

@RestControllerAdvice
class ExceptionHandlers {

    private val log = LoggerFactory.getLogger(javaClass)

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WebExchangeBindException::class)
    fun handleBindExceptions(ex: WebExchangeBindException) : Map<String, Any?>? {
        log.warn(ex.toString())
        val fieldErrors = ex.fieldErrors
        val errors: Map<String, String?> = fieldErrors.map {it.field to it.defaultMessage}.toMap()
        return mapOf(
                Pair("message", "There were errors validating your request"),
                Pair("errors", errors))
    }

}