package io.opendmp.sdk.plugin.process

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import io.opendmp.sdk.plugin.Utils
import org.apache.camel.Exchange
import java.util.*

interface OdmpProcessor {

    fun process(exchange: Exchange)

    fun decodeProperties(exchange: Exchange) {
        val headers = exchange.getIn().headers
        val propEnc = headers["properties"].toString()
        val jsonStr = String(Base64.getUrlDecoder().decode(propEnc))
        val props = Utils.mapper.readValue<Map<String, Any?>>(jsonStr)
        exchange.getIn().headers = props
    }
}