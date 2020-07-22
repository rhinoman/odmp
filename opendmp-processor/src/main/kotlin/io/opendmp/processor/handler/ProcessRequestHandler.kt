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

package io.opendmp.processor.handler

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.opendmp.common.exception.NotImplementedException
import io.opendmp.common.message.ProcessRequestMessage
import io.opendmp.common.model.ProcessorType
import io.opendmp.processor.ingest.IngestUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ProcessRequestHandler {

    private val log = LoggerFactory.getLogger(ProcessRequestHandler::class.java)

    private val mapper = jacksonObjectMapper()

    fun receiveProcessRequest(data: String) {
        log.debug("Received message")
        try {
            val msg = mapper.readValue<ProcessRequestMessage>(data)
            when(msg.processorType) {
                ProcessorType.INGEST -> IngestUtils.handleIngestRequest(msg)
                else -> throw NotImplementedException("${msg.processorType} is not yet implemented")
            }
        } catch (jpe: JsonProcessingException) {
            log.error("Error extracting message", jpe)
        } catch (ex: Exception) {
            log.error("Error executing processor", ex)
        } finally {
                //TODO: Send a response message here
        }
    }
}