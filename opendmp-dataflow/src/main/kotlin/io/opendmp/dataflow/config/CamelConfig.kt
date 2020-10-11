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

package io.opendmp.dataflow.config

import org.apache.camel.component.ribbon.RibbonConfiguration
import org.apache.camel.component.ribbon.cloud.RibbonServiceLoadBalancer
import org.apache.camel.model.cloud.ServiceCallConfigurationDefinition
import org.apache.camel.processor.loadbalancer.RoundRobinLoadBalancer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CamelConfig() {

    @Value("\${spring.cloud.consul.host}")
    lateinit var consulHost: String
    @Value("\${spring.cloud.consul.port}")
    lateinit var consulPort: String
    @Value("\${spring.cloud.consul.scheme}")
    lateinit var consulScheme: String

    @Bean
    fun basicServiceCallConfiguration() : ServiceCallConfigurationDefinition {
        val conf = ServiceCallConfigurationDefinition()
        val consulUrl = "$consulScheme://$consulHost:$consulPort"
        conf
                .component("netty-http")
                .consulServiceDiscovery()
                .url(consulUrl)
                .blockSeconds(5)
                .connectTimeoutMillis(1000)
                .readTimeoutMillis(1000)
                .writeTimeoutMillis(1000)
        return conf
    }
}