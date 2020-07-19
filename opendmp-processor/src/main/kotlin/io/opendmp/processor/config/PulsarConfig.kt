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

package io.opendmp.processor.config

import org.apache.camel.CamelContext
import org.apache.camel.component.pulsar.PulsarComponent
import org.apache.camel.component.pulsar.utils.AutoConfiguration
import org.apache.pulsar.client.admin.PulsarAdmin
import org.apache.pulsar.client.api.PulsarClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PulsarConfig @Autowired constructor(private val camelContext: CamelContext) {

    @Value("\${odmp.pulsar.admin.url}")
    private val adminUrl: String = "http://localhost:8080"

    @Value("\${odmp.pulsar.client.url}")
    private val clientUrl: String  = "pulsar://localhost:6650"

    @Value("\${odmp.pulsar.clusters}")
    private val clusters: Set<String> = setOf("standalone")

    @Bean
    fun pulsarClient() : PulsarClient {
        return PulsarClient.builder().serviceUrl(clientUrl).build()
    }

    @Bean
    fun pulsarAdmin() : PulsarAdmin {
        return PulsarAdmin.builder().serviceHttpUrl(adminUrl).build()
    }

    @Bean
    fun pulsarAutoConfig() : AutoConfiguration {
        return AutoConfiguration(pulsarAdmin(), clusters)
    }

    @Bean
    fun pulsar() : PulsarComponent {
        val pc = PulsarComponent(camelContext)
        pc.autoConfiguration = pulsarAutoConfig()
        pc.pulsarClient = pulsarClient()
        return pc
    }

}