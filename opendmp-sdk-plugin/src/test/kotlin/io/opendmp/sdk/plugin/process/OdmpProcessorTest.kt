package io.opendmp.sdk.plugin.process

import io.opendmp.common.util.MessageUtil
import org.apache.camel.Exchange
import org.apache.camel.support.DefaultExchange
import org.apache.camel.test.junit5.CamelTestSupport
import org.junit.jupiter.api.Test
import java.util.*

class OdmpProcessorTest(): CamelTestSupport() {


    private fun getQueryParams(properties: Map<String, Any>): String {
        val propStr = MessageUtil.mapper.writeValueAsString(properties)
        val b64Props = Base64.getUrlEncoder().encodeToString(propStr.toByteArray())
        return b64Props
    }

    val testProcessor = TestProcessor()

    @Test
    fun `should decode headers`() {
        val exchange = DefaultExchange(context())

        val properties = mapOf(
                "command" to "ls",
                "sudo" to true,
                "arguments" to "-al",
                "timeout" to 5000,
                "code" to "(fn [x] (if (= x 3) (* x 3) x))")

        val qString = getQueryParams(properties)
        exchange.getIn().setHeader("properties", qString)
        val decodedProps = testProcessor.decodeProperties(exchange)
        val decodedHeaders = exchange.getIn().headers
        println(decodedHeaders["command"].toString())
    }
}

class TestProcessor: OdmpProcessor {
    override fun process(exchange: Exchange) {
        TODO("Not yet implemented")
    }
}