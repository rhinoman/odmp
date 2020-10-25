package io.opendmp.plugin.clojure.process

import io.opendmp.sdk.plugin.process.OdmpProcessor
import org.apache.camel.Exchange
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ClojureProcessor : OdmpProcessor {

    val log = LoggerFactory.getLogger(javaClass)

    override fun process(exchange: Exchange) {
        val headers = exchange.getIn().headers
        val code = headers["code"].toString()
        val language = headers["language"].toString()

        val executor = ClojureExecutor()

        log.info("Executing Clojure script")

        val result = executor.executeScript(code, exchange.getIn().getBody(ByteArray::class.java))

        exchange.getIn().body = result
    }
}