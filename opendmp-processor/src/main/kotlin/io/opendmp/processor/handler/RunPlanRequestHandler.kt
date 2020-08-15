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
import io.opendmp.common.exception.RunPlanConflictException
import io.opendmp.common.message.StartRunPlanRequestMessage
import io.opendmp.common.message.StopRunPlanRequestMessage
import io.opendmp.processor.domain.RunPlan
import io.opendmp.processor.domain.RunPlanRecord
import io.opendmp.processor.run.RunPlanRouteBuilder
import org.apache.camel.CamelContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate

import org.springframework.stereotype.Component

@Component
class RunPlanRequestHandler(
        @Autowired private val camelContext: CamelContext,
        @Autowired private val rpTemplate: RedisTemplate<String, RunPlanRecord>) {

    private val log = LoggerFactory.getLogger(RunPlanRequestHandler::class.java)

    private val mapper = jacksonObjectMapper()

    suspend fun receiveStartRequest(data: String) {
        log.debug("Received START message")
        try {
            val msg = mapper.readValue<StartRunPlanRequestMessage>(data)
            //stash runplan in redis
            log.info("Received Run Plan: ${msg.requestId}")
            //Check if Run plan is already loaded
            if (rpTemplate.opsForValue().get(msg.requestId) != null) {
                throw RunPlanConflictException("Run Plan is already running")
            }
            val rp = RunPlan.fromStartRunPlanRequestMessage(msg)
            //Store the run plan to Redis
            rpTemplate.opsForValue().set(rp.id, RunPlanRecord.fromRunPlan(rp))
            val routeBuilder = RunPlanRouteBuilder(rp)
            camelContext.addRoutes(routeBuilder)
        } catch (jpe: JsonProcessingException) {
            log.error("Error extracting message", jpe)
        } catch (ex: Exception) {
            log.error("Error executing processor", ex)
        } finally {
                //TODO: Send a response message here
        }
    }

    suspend fun receiveStopRequest(data: String) {
        log.debug("Received STOP message")
        try {
            val msg = mapper.readValue<StopRunPlanRequestMessage>(data)
            log.info("Received Stop request for Run Plan: ${msg.runPlanId}")
            val rpRec = rpTemplate.opsForValue().get(msg.runPlanId)
                    ?: throw RunPlanConflictException("Couldn't find Run Plan in cache")
            camelContext.routes
                    .filter { it.id.startsWith(rpRec.id) }
                    .forEach {
                        camelContext.routeController.stopRoute(it.routeId)
                        val remd = camelContext.removeEndpoints(".*://${msg.runPlanId}.*")
                        log.info("Stopped ${remd.size} endpoints for route ${it.id}")
                        camelContext.removeRoute(it.routeId)

                    }
            rpTemplate.delete(rpRec.id)
        } catch (jpe: JsonProcessingException) {
            log.error("Error extracting message", jpe)
        } catch (ex: Exception) {
            log.error("Error on stop message", ex)
        }
    }
}