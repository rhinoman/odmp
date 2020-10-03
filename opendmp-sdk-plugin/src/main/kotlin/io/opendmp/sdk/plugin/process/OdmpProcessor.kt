package io.opendmp.sdk.plugin.process

import org.apache.camel.Exchange

interface OdmpProcessor {
    fun process(exchange: Exchange)
}