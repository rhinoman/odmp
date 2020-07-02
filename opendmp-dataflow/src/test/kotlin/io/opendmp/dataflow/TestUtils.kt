package io.opendmp.dataflow

import org.springframework.context.annotation.PropertySource
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

class TestUtils {

    fun genUserHeaders(username: String) : HttpHeaders {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.contentType = MediaType.APPLICATION_JSON
        return headers
    }

    fun<T> syncSave(template: ReactiveMongoTemplate, data: T) : Mono<T> {
        return template.save(data)
    }

}