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

package io.opendmp.dataflow.service

import com.mongodb.client.result.DeleteResult
import io.opendmp.dataflow.Util
import io.opendmp.dataflow.api.exception.NotFoundException
import io.opendmp.dataflow.api.exception.ResourceConflictException
import io.opendmp.dataflow.api.request.CreateCollectionRequest
import io.opendmp.dataflow.api.response.CountResponse
import io.opendmp.dataflow.model.CollectionModel
import io.opendmp.dataflow.model.DatasetModel
import io.opendmp.dataflow.model.ProcessorModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class CollectionService (private val mongoTemplate: ReactiveMongoTemplate) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun createCollection(data: CreateCollectionRequest,
                         authentication: Authentication) : Mono<CollectionModel> {

        val username = Util.getUsername(authentication)

        val collection = CollectionModel(
                name = data.name!!,
                description = data.description,
                group = data.group,
                creator = username
        )
        return mongoTemplate.save<CollectionModel>(collection)
    }

    fun get(id: String) : Mono<CollectionModel> {
        return mongoTemplate.findById<CollectionModel>(id)
    }

    suspend fun getList() : Flow<CollectionModel> {
        return mongoTemplate.findAll<CollectionModel>().asFlow()
    }

    suspend fun getDatasets(collectionId: String,
                            maxPerPage: Int,
                            page: Int,
                            sortBy: String?,
                            sortDir: Sort.Direction?) : Flow<DatasetModel> {

        val query = Query(Criteria.where("collectionId").isEqualTo(collectionId))
        if(sortBy != null) {
            query.with(Sort.by(sortDir ?: Sort.Direction.DESC, sortBy))
        }
        query.limit(maxPerPage)
        query.skip((maxPerPage * page).toLong())

        return mongoTemplate.find<DatasetModel>(query).asFlow()
    }

    fun countDatasets(collectionId: String) : Mono<CountResponse> {
        return mongoTemplate.count<DatasetModel>(
                Query(Criteria.where("collectionId").isEqualTo(collectionId))).map { CountResponse(it) }
    }

    fun delete(id: String) : Mono<DeleteResult> {

        return mongoTemplate.findById<CollectionModel>(id)
                .switchIfEmpty { throw NotFoundException() }
                .flatMap {
                    val query = Query(Criteria.where("properties.collection").isEqualTo(id))
                    mongoTemplate.count<ProcessorModel>(query)
                }.flatMap { num ->
                    if(num > 0) {
                        val prefix = if(num == 1L) "One processor is" else "$num processors are"
                        Mono.error(ResourceConflictException("$prefix still referencing this collection!"))
                    } else {
                        mongoTemplate.remove<CollectionModel>(Query(Criteria.where("id").isEqualTo(id)))
                    }
                }.flatMap {
                        mongoTemplate.remove<DatasetModel>(Query(Criteria.where("collectionId").isEqualTo(id)))
                }

    }

}