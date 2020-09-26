package io.opendmp.sdk.plugin.routes

import io.opendmp.sdk.plugin.config.PluginConfigurationProvider
import io.opendmp.sdk.plugin.process.OdmpProcessor
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.dataformat.JsonLibrary
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class RegisterPlugin(
        private val pluginConfigurationProvider: PluginConfigurationProvider,
        private val odmpProcessor: OdmpProcessor
) : RouteBuilder() {

    @Value("\${odmp.plugin.host:0.0.0.0}")
    lateinit var hostname: String

    @Value("\${odmp.plugin.port:8004}")
    lateinit var port: String

    @Value("\${odmp.plugin.name}")
    lateinit var pluginName: String

    override fun configure() {
        val id = UUID.randomUUID().toString()
        from("service:$pluginName:netty-http:http://0.0.0.0:$port/config?service.id=$id")
                .bean(pluginConfigurationProvider, "pluginConfiguration")
                .marshal().json(JsonLibrary.Jackson)
                .setHeader("Content-Type", constant("application/json"))

        from("service:$pluginName:netty-http:http://0.0.0.0:$port/process?service.id=$id")
                .bean(odmpProcessor, "process")

    }
}