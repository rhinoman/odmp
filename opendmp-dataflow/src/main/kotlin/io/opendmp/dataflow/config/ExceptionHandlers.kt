/*
 * Copyright (c) 2020. The Open Data Management Platform contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opendmp.dataflow.config

import io.opendmp.dataflow.api.exception.ResourceConflictException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDateTime

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

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ResourceConflictException::class)
    fun handleConflictException(ex: ResourceConflictException) : Map<String, Any> {
        log.warn(ex.toString())
        return mapOf(
                Pair("status", 409),
                Pair("error", "conflict"),
                Pair("timestamp", LocalDateTime.now()),
                Pair("message", ex.message ?: "Could not complete your request due to a resource conflict.")
        )
    }

}