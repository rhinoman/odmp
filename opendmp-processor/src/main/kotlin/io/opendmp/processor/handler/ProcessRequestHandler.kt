package io.opendmp.processor.handler

import org.springframework.stereotype.Component

@Component
class ProcessRequestHandler {

    fun receiveProcessRequest(data: String) {
        val msg = data.toString()
        println("RECEIVED: $msg")
    }

}