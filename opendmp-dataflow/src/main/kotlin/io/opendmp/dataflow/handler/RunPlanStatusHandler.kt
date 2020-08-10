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
import io.opendmp.common.util.MessageUtil
import io.opendmp.dataflow.api.exception.NotFoundException
import io.opendmp.dataflow.model.DatasetModel
import io.opendmp.dataflow.service.CollectionService
import io.opendmp.dataflow.service.DataflowService
import io.opendmp.dataflow.service.DatasetService
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import org.apache.camel.CamelContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Component
class RunPlanStatusHandler(
        @Autowired private val camelContext: CamelContext,
        @Autowired private val datasetService: DatasetService) {

    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun receiveCollectStatus(data: String) : Mono<DatasetModel>? {
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

}