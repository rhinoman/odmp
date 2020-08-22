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

package io.opendmp.dataflow.handler

import com.fasterxml.jackson.datatype.jsr310.JSR310Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.opendmp.common.exception.CollectProcessorException
import io.opendmp.common.message.CollectionCompleteMessage
import io.opendmp.common.message.RunPlanFailureMessage
import io.opendmp.common.util.MessageUtil
import io.opendmp.dataflow.api.exception.NotFoundException
import io.opendmp.dataflow.model.DatasetModel
import io.opendmp.dataflow.model.runplan.RunError
import io.opendmp.dataflow.service.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import org.apache.camel.CamelContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Component
import reactor.core.Disposable
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

@Component
class RunPlanStatusHandler(
        @Autowired private val camelContext: CamelContext,
        @Autowired private val datasetService: DatasetService,
        @Autowired private val runPlanService: RunPlanService) {

    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun receiveCollectStatus(data: String): Disposable?  {
        log.debug("Received COLLECT status")
        try {
            // Nothing to do if we can't extract the message, eh?
            val msg = MessageUtil.extractMessageFromString<CollectionCompleteMessage>(data) ?: return null
            return datasetService.createDataset(msg)
        } catch (ex: Exception) {
            log.error("Error processing collection completion", ex)
            return null
        }
    }

    suspend fun receiveFailureStatus(data: String) {
        log.debug("Received FAILURE status")
        try {
            val msg = MessageUtil.extractMessageFromString<RunPlanFailureMessage>(data)
            runPlanService.get(msg!!.runPlanId).subscribe {rp ->
                val err = RunError(
                        id = msg.requestId,
                        processorId = msg.processorId,
                        errorMessage = msg.errorMessage,
                        fromId = msg.fromId,
                        time = msg.time)
                rp.errors[err.id] = err
                runPlanService.updateRunPlan(rp).block()
            }
        } catch (ex: Exception) {
            log.error("Error processing collection completion", ex)
        }
    }

}