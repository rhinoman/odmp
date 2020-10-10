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

    override fun configure() {
        val cid = UUID.randomUUID().toString()
        val pid = UUID.randomUUID().toString()

        from("servlet://config")
                .bean(pluginConfigurationProvider, "pluginConfiguration")
                .marshal().json(JsonLibrary.Jackson)
                .setHeader("Content-Type", constant("application/json"))

        from("servlet://process?bridgeErrorHandler=true")
                .bean(odmpProcessor, "decodeProperties")
                .bean(odmpProcessor, "process")

    }
}