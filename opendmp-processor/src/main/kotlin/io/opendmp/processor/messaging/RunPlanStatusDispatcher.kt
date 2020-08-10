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

package io.opendmp.processor.messaging

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.opendmp.common.message.CollectionCompleteMessage
import org.apache.camel.ProducerTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class RunPlanStatusDispatcher @Autowired constructor(
        private val producerTemplate: ProducerTemplate
){

    @Value("\${odmp.pulsar.namespace")
    lateinit var pulsarNamespace: String

    private val log = LoggerFactory.getLogger(javaClass)

    private val mapper = jacksonObjectMapper()

    init {
        mapper.findAndRegisterModules()
    }

    fun collectEndPoint(): String =
            "pulsar:persistent://$pulsarNamespace/runplan_collect_status?producerName=odmpProducer"

    private fun sendMessage(msg: Any, endpoint: String) {
        try {
            val jsonData = mapper.writeValueAsString(msg)
            producerTemplate.sendBody(endpoint, jsonData)
        } catch (jpe: JsonProcessingException) {
            log.error("Error converting message", jpe)
        } catch (ex: Exception) {
            log.error("Error sending message", ex)
        }
    }

    suspend fun sendCollectionComplete(msg: CollectionCompleteMessage) {
        sendMessage(msg, collectEndPoint())
    }
}