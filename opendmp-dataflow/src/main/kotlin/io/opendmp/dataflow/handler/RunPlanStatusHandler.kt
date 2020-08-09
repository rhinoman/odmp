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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.opendmp.common.message.CollectionCompleteMessage
import io.opendmp.common.util.MessageUtil
import io.opendmp.dataflow.model.DatasetModel
import org.apache.camel.CamelContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RunPlanStatusHandler(
        @Autowired private val camelContext: CamelContext) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val mapper = jacksonObjectMapper()

    suspend fun receiveCollectStatus(data: String) {
        log.debug("Received COLLECT status")
        try {
            // Nothing to do if we can't extract the message, eh?
            val msg = MessageUtil.extractMessageFromString<CollectionCompleteMessage>(data) ?: return
            val dataset = DatasetModel(
                    collectionId = msg.collectionId,
                    dataflowId = msg.flowId,
                    destinationType = msg.destinationType,
                    location = msg.location,
                    createdOn = msg.timeStamp)
        } catch (ex: Exception) {
            log.error("Error processing collection completion", ex)
        }
    }

}