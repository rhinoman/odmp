/*
 * Copyright (c) 2020. The Open Data Management Platform contributors.
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

import com.ecwid.consul.v1.ConsulClient
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.opendmp.common.model.plugin.PluginConfiguration
import io.opendmp.common.util.MessageUtil
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.consul.ConsulClientConfiguration
import org.apache.camel.component.consul.ConsulConfiguration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class PluginService : RouteBuilder() {

    private val mapper = MessageUtil.mapper
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${odmp.plugins-enabled}")
    lateinit var enabledPlugins: List<String>

    fun processConfig(data: String) {
        val pc: PluginConfiguration = mapper.readValue<PluginConfiguration>(data)
        add(pc.serviceName, pc)
        log.info("Loaded config for plugin ${pc.displayName}")
    }

    override fun configure() {
        enabledPlugins.forEach {
            from("timer://runOnce?repeatCount=1")
                    .serviceCall()
                      .name(it)
                      .uri("$it/config")
                      .component("netty-http")
                      .end()
                    .bean(javaClass, "processConfig")
                    .log("Plugin configs loaded")
        }
    }
    companion object {
        private val plugins: MutableMap<String, PluginConfiguration> = mutableMapOf()

        @Synchronized
        fun add(name: String, config: PluginConfiguration) {
            plugins[name] = config
        }

        fun get() : Map<String, PluginConfiguration> = plugins

        fun get(name: String) = plugins[name]

        @Synchronized
        fun remove(name: String) : PluginConfiguration? {
            return plugins.remove(name)
        }
    }
}