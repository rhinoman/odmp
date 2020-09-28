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

package io.opendmp.processor.integration

import io.opendmp.common.model.ProcessorRunModel
import io.opendmp.common.model.ProcessorType
import io.opendmp.processor.TestUtils
import io.opendmp.processor.config.RedisConfig
import io.opendmp.processor.config.SpringContext
import io.opendmp.processor.domain.DataEnvelope
import io.opendmp.processor.handler.RunPlanRequestHandler
import io.opendmp.processor.messaging.RunPlanRequestRouter
import io.opendmp.processor.messaging.RunPlanStatusDispatcher
import io.opendmp.processor.run.processors.PluginProcessor
import org.apache.camel.CamelContext
import org.apache.camel.EndpointInject
import org.apache.camel.FluentProducerTemplate
import org.apache.camel.ProducerTemplate
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.support.DefaultExchange
import org.apache.camel.test.spring.junit5.CamelSpringBootTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.EnabledIf
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@SpringBootTest
@ExtendWith(SpringExtension::class)
//@ContextConfiguration
@CamelSpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnabledIf(expression = "\${odmp.integration-tests.enabled}", loadContext = true)
class TestPluginProcessor @Autowired constructor (
        val camelContext: CamelContext,
        val producerTemplate: FluentProducerTemplate){

    @MockBean
    lateinit var  redisConfig: RedisConfig

    @MockBean
    lateinit var runPlanRequestHandler: RunPlanRequestHandler

    @MockBean
    lateinit var runPlanRequestRouter: RunPlanRequestRouter

    @MockBean
    lateinit var runPlanStatusDispatcher: RunPlanStatusDispatcher


    @Test
    fun testPluginProcessor(){
        val exchange = DefaultExchange(camelContext)
        val properties: Map<String, Any> = mapOf(
                "serviceName" to "opendmp-ffmpeg-plugin")
        exchange.getIn().body = "I'm Data!".toByteArray()
        exchange.setProperty("dataEnvelope", TestUtils.createDataEnvelope())
        val processor = ProcessorRunModel(
                id = UUID.randomUUID().toString(),
                flowId = UUID.randomUUID().toString(),
                inputs = listOf(),
                name = "Test Plugin Processor",
                type = ProcessorType.PLUGIN,
                properties = properties)

        val pluginProcessor = PluginProcessor(processor)
        pluginProcessor.process(exchange)
        val envelopeOut = exchange.getProperty("dataEnvelope") as DataEnvelope
        val data = exchange.getIn().getBody(ByteArray::class.java)
        assertNotNull(data)

    }

}