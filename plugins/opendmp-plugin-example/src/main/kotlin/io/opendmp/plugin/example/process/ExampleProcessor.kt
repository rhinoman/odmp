package io.opendmp.plugin.example.process

import io.opendmp.common.util.CommandUtil
import io.opendmp.sdk.plugin.process.OdmpProcessor
import org.apache.camel.Exchange
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ExampleProcessor : OdmpProcessor {

    val log = LoggerFactory.getLogger(javaClass)

    override fun process(exchange: Exchange) {
        val headers = exchange.getIn().headers
        val command = headers["command"].toString()
        val sudo = headers["sudo"].toString().toBoolean()
        val arguments = headers["arguments"].toString()
        val timeout = headers["timeout"].toString().toLong()

        log.info("Executing command $command")

        var commandLine = "$command $arguments"
        if(sudo) commandLine = "sudo $commandLine"
        val result =
                CommandUtil.runCommand(
                        command = commandLine,
                        data = exchange.getIn().getBody(ByteArray::class.java),
                        timeoutSeconds = timeout)

        exchange.getIn().body = result
    }


}