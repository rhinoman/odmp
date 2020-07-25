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
import io.opendmp.common.message.StartRunPlanRequestMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class RunPlanRequestHandler {

    private val log = LoggerFactory.getLogger(RunPlanRequestHandler::class.java)

    private val mapper = jacksonObjectMapper()

    fun receiveRunPlanRequest(data: String) {
        log.debug("Received message")
        try {
            val msg = mapper.readValue<StartRunPlanRequestMessage>(data)
            log.info(msg.toString())
        } catch (jpe: JsonProcessingException) {
            log.error("Error extracting message", jpe)
        } catch (ex: Exception) {
            log.error("Error executing processor", ex)
        } finally {
                //TODO: Send a response message here
        }
    }
}