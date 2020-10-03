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

package io.opendmp.processor.run.processors

import io.opendmp.common.exception.CommandExecutionException
import io.opendmp.common.model.ProcessorRunModel
import io.opendmp.common.model.ProcessorType
import io.opendmp.processor.TestUtils
import io.opendmp.processor.config.RedisConfig
import io.opendmp.processor.domain.DataEnvelope
import io.opendmp.processor.handler.RunPlanRequestHandler
import io.opendmp.processor.messaging.RunPlanRequestRouter
import io.opendmp.processor.messaging.RunPlanStatusDispatcher
import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.support.DefaultExchange
import org.apache.camel.test.spring.junit5.CamelSpringBootTest
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistration
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@SpringBootTest
@CamelSpringBootTest
@ExtendWith(SpringExtension::class)
@ContextConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TestExternalProcessor @Autowired constructor(
        private val testCamelContext: CamelContext
) {

    @Test
    fun `external processor should run successfully`() {
        val exchange = DefaultExchange(testCamelContext)
        val properties: Map<String, Any> = mapOf(
                "command" to "wc -c",
                "timeout" to "10")
        exchange.getIn().body = "I'm Data!"
        exchange.setProperty("dataEnvelope", TestUtils.createDataEnvelope())
        val processor = ProcessorRunModel(
                id = UUID.randomUUID().toString(),
                flowId = UUID.randomUUID().toString(),
                inputs = listOf(),
                name = "Test External Processor",
                type = ProcessorType.EXTERNAL,
                properties = properties)

        val externalProcessor = ExternalProcessor(processor)
        externalProcessor.process(exchange)

        val envelopeOut = exchange.getProperty("dataEnvelope") as DataEnvelope
        val data = exchange.getIn().getBody(ByteArray::class.java)
        val str = data.decodeToString().trim()
        assertNotNull(data)
        assertEquals(9, Integer.parseInt(str))
        assertEquals(2, envelopeOut.history.size)
    }

    @Test
    fun `external processor should handle errors!`(){
        val exchange = DefaultExchange(testCamelContext)
        val properties: Map<String, Any> = mapOf(
                "command" to "wc -foobar",
                "timeout" to "10")

        exchange.getIn().body = "I'm Data!"
        exchange.setProperty("dataEnvelope", TestUtils.createDataEnvelope())
        val processor = ProcessorRunModel(
                id = UUID.randomUUID().toString(),
                flowId = UUID.randomUUID().toString(),
                inputs = listOf(),
                name = "Test External Processor",
                type = ProcessorType.EXTERNAL,
                properties = properties)

        val externalProcessor = ExternalProcessor(processor)
        assertThrows(CommandExecutionException::class.java) {
            externalProcessor.process(exchange)
        }

    }


}