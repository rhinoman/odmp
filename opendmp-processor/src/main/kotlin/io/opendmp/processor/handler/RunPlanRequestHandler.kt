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

package io.opendmp.processor.handler

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.opendmp.common.exception.RunPlanConflictException
import io.opendmp.common.message.RunPlanStartFailureMessage
import io.opendmp.common.message.StartRunPlanRequestMessage
import io.opendmp.common.message.StopFlowRequestMessage
import io.opendmp.common.message.StopRunPlanRequestMessage
import io.opendmp.processor.domain.RunPlan
import io.opendmp.processor.domain.RunPlanRecord
import io.opendmp.processor.messaging.RunPlanStatusDispatcher
import io.opendmp.processor.run.RunPlanRouteBuilder
import io.opendmp.processor.run.RunningDataflows
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope.coroutineContext
import org.apache.camel.CamelContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate

import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.suspendCoroutine

@Component
class RunPlanRequestHandler(
        @Autowired private val camelContext: CamelContext,
        @Autowired private val runPlanStatusDispatcher: RunPlanStatusDispatcher,
        @Autowired private val rpTemplate: RedisTemplate<String, RunPlanRecord>) {

    private val log = LoggerFactory.getLogger(RunPlanRequestHandler::class.java)

    private val mapper = jacksonObjectMapper()

    init {
        mapper.findAndRegisterModules()
    }

    suspend fun receiveStartRequest(data: String) {
        log.debug("Received START message")
        var msg: StartRunPlanRequestMessage? = null
        try {
            msg = mapper.readValue<StartRunPlanRequestMessage>(data)
            //stash runplan in redis
            log.info("Received Run Plan: ${msg.requestId}")
            //Check if Run plan is already loaded
            if (rpTemplate.opsForValue().get(msg.flowId) != null) {
                throw RunPlanConflictException("This dataflow is already running")
            }
            val rp = RunPlan.fromStartRunPlanRequestMessage(msg)
            //Store the run plan to Redis
            rpTemplate.opsForValue().set(rp.flowId, RunPlanRecord.fromRunPlan(rp))
            //Keep track of this flowId
            RunningDataflows.add(rp.flowId, rp.id)
            val routeBuilder = RunPlanRouteBuilder(rp)
            camelContext.addRoutes(routeBuilder)

        } catch (jpe: JsonProcessingException) {
            log.error("Error extracting message", jpe)
        } catch (rpce: RunPlanConflictException) {
            log.warn(rpce.localizedMessage)
        } catch (ex: Exception) {
            log.error("Error building run plan", ex)
            runPlanStatusDispatcher.sendStartRunPlanFailureMessage(
                    RunPlanStartFailureMessage(
                            requestId = UUID.randomUUID().toString(),
                            runPlanId = msg!!.requestId,
                            errorMessage = "Could not start Run Plan: ${ex.localizedMessage}",
                            time = Instant.now()))
            // Delete the run plan from redis if it's there
            rpTemplate.delete(msg.flowId)
        }
    }

    /**
     * Sometimes a Runplan gets stuck in the cache even though it's no longer running.
     * Usually this happens when a ProcessorService instance doesn't shut down gracefully.
     *
     * So we have a record in the cache with no "owning" processor service.
     * This waits a couple seconds after a stop request and clears the record
     * from the cache if it's still there (i.e., the owning process didn't kill it).
     */
    suspend fun checkForZombieAsync(runPlanRecord: RunPlanRecord) {
        delay(2000L)
        val curRec = rpTemplate.opsForValue().get(runPlanRecord.flowId)
        if (curRec != null && curRec.timestamp == runPlanRecord.timestamp) {
            log.info("Killing Zombie RunPlan record")
            rpTemplate.delete(runPlanRecord.flowId)
        }
    }

    suspend fun receiveStopRequest(data: String) {
        log.debug("Received STOP message")
        try {
            val msg = mapper.readValue<StopFlowRequestMessage>(data)
            log.info("Received Stop request for Dataflow: ${msg.flowId}")
            val rpRec = rpTemplate.opsForValue().get(msg.flowId)
            if(RunningDataflows.get(msg.flowId) == null) {
                log.info("Dataflow not running on this instance.")
                if(rpRec != null) {
                    CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                        checkForZombieAsync(rpRec)
                    }
                }
                return
            }
            camelContext.routes
                    .filter { it.id.startsWith(rpRec!!.id) }
                    .forEach {
                        camelContext.routeController.stopRoute(it.routeId)
                        val remd = camelContext.removeEndpoints(".*://${rpRec!!.id}.*")
                        log.info("Stopped ${remd.size} endpoints for route ${it.id}")
                        camelContext.removeRoute(it.routeId)
                    }
            rpTemplate.delete(msg.flowId)
            RunningDataflows.remove(msg.flowId)
        } catch (jpe: JsonProcessingException) {
            log.error("Error extracting message", jpe)
        } catch (ex: Exception) {
            log.error("Error on stop message", ex)
        }
    }
}