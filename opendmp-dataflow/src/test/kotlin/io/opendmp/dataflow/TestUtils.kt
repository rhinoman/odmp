package io.opendmp.dataflow

import org.springframework.context.annotation.PropertySource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

class TestUtils {

    fun genUserHeaders(username: String) : HttpHeaders {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.contentType = MediaType.APPLICATION_JSON
        return headers
    }

    fun <T> dataEntity(headers: HttpHeaders, data: T) : HttpEntity<T> {
        return HttpEntity(data, headers)
    }
}