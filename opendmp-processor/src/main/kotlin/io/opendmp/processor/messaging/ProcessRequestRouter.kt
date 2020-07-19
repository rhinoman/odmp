package io.opendmp.processor.messaging

import io.opendmp.processor.handler.ProcessRequestHandler
import org.apache.camel.BeanInject
import org.apache.camel.CamelContext
import org.apache.camel.ConsumerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.pulsar.PulsarComponent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ProcessRequestRouter(
        @Autowired val processRequestHandler: ProcessRequestHandler,
        @Autowired val camel: CamelContext,
        @Autowired val consumer: ConsumerTemplate
) : RouteBuilder() {

    @Value("\${odmp.pulsar.namespace}")
    val pulsarNamespace: String = "public/default"

    fun endPointUrl() : String =
            "pulsar:persistent://$pulsarNamespace/process_request"

    override fun configure() {
        from(endPointUrl()).to("bean:processRequestHandler")
    }

}