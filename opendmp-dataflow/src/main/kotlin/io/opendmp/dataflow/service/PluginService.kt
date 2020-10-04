/*
 * Copyright (c) 2020. James Adam and the Open Data Management Platform contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opendmp.dataflow.service

import com.fasterxml.jackson.module.kotlin.readValue
import io.opendmp.common.model.plugin.PluginConfiguration
import io.opendmp.common.util.MessageUtil
import org.apache.camel.Exchange
import org.apache.camel.Headers
import org.apache.camel.LoggingLevel
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.ribbon.RibbonConfiguration
import org.apache.camel.model.cloud.ServiceCallConfigurationDefinition
import org.apache.camel.processor.errorhandler.DeadLetterChannel
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Service
@Profile("!test")
class PluginService(private val producerTemplate: ProducerTemplate) : RouteBuilder() {

    private val mapper = MessageUtil.mapper
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${odmp.plugins-enabled}")
    lateinit var enabledPlugins: List<String>

    fun processConfig(exchange: Exchange): PluginConfiguration {
        val data = exchange.getIn().getBody(String::class.java)
        val pc: PluginConfiguration = mapper.readValue<PluginConfiguration>(data)
        log.info("Loaded config for plugin ${pc.displayName}")
        return pc
    }

    fun handleError(@Headers headers: Map<String, Any>, exchange: Exchange, cause: Exception) {
        val pluginName = exchange.getIn().body.toString()
        log.warn("Couldn't connect to plugin $pluginName instance")
        exchange.getIn().body = cause
    }

    override fun configure() {

        errorHandler(deadLetterChannel("direct:plugin-dead")
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .maximumRedeliveries(0))

        onException(Exception::class.java).to("direct:plugin-dead")

        from("direct:plugin-dead")
                .bean(javaClass, "handleError")

        from("direct:plugin-config")
                  .serviceCall()
                    .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                    .name("\$simple{body}")
                    .uri("\$simple{body}/config?bridgeEndpoint=true")
                    .serviceCallConfiguration("basicServiceCallConfiguration")
                    .bean(javaClass,"processConfig").end()

    }

    fun getConfigs(): Map<String, PluginConfiguration?> {

            return enabledPlugins.map {
                try {
                    it to producerTemplate.asyncRequestBody("direct:plugin-config?bridgeErrorHandler=true", it, PluginConfiguration::class.java)
                            .get(1, TimeUnit.SECONDS)
                } catch (te: TimeoutException) {
                    log.warn("Couldn't get a configuration for $it")
                    it to null
                }
            }.toMap()
    }
}