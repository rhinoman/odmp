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

package io.opendmp.dataflow.service

import com.mongodb.client.result.DeleteResult
import io.opendmp.common.exception.CollectProcessorException
import io.opendmp.common.message.CollectionCompleteMessage
import io.opendmp.dataflow.Util
import io.opendmp.dataflow.model.DatasetModel
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Service
class DatasetService (private val mongoTemplate: ReactiveMongoTemplate,
                      private val dataflowService: DataflowService,
                      private val collectionService: CollectionService) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun createDataset(msg: CollectionCompleteMessage) : Mono<DatasetModel> {
        val collection = collectionService.get(msg.collectionId)
                .switchIfEmpty { throw CollectProcessorException("the specified collection does not exist.") }
        val dataflow = dataflowService.get(msg.flowId)
                .switchIfEmpty { throw CollectProcessorException("the specified dataflow does not exist") }

       return Mono.zip(collection, dataflow).flatMap { xs ->

            val flowName = xs.t2.name
            val time = msg.timeStamp
            val fmt = DateTimeFormatter
                    .ofPattern("yyyyDDDHHmmss.S")
                    .withZone(ZoneId.systemDefault())
            val dataset = DatasetModel(
                    name = "$flowName-${fmt.format(time)}",
                    collectionId = msg.collectionId,
                    dataflowId = msg.flowId,
                    destinationType = msg.destinationType,
                    location = msg.location,
                    createdOn = msg.timeStamp)
            mongoTemplate.save(dataset)
        }

    }

    fun get(id: String) : Mono<DatasetModel> {
        return mongoTemplate.findById(id)
    }

    fun delete(id: String) : Mono<DeleteResult> {
        val query = Query(Criteria.where("id").isEqualTo(id))
        return mongoTemplate.remove<DatasetModel>(query)
    }

}