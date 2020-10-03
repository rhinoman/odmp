package io.opendmp.plugin.ffmpeg.process

import io.opendmp.common.util.CommandUtil
import io.opendmp.sdk.plugin.process.OdmpProcessor
import org.apache.camel.Exchange
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FFMPEGProcessor : OdmpProcessor {

    val log = LoggerFactory.getLogger(javaClass)

    override fun process(exchange: Exchange) {
        val headers = exchange.getIn().headers
        val command = headers["command"].toString()
        val timeout = headers["timeout"].toString().toLong()

        log.info("Executing command $command")

        val result =
                CommandUtil.runCommand(
                        command = "ffmpeg -i - $command pipe:",
                        data = exchange.getIn().getBody(ByteArray::class.java),
                        timeoutSeconds = timeout)

        exchange.getIn().body = result
    }


}