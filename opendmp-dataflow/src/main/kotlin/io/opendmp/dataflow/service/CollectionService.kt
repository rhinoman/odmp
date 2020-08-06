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
import io.opendmp.dataflow.Util
import io.opendmp.dataflow.api.request.CreateCollectionRequest
import io.opendmp.dataflow.model.CollectionModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.remove
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

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

    fun delete(id: String) : Mono<DeleteResult> {
        val query = Query(Criteria.where("id").isEqualTo(id))
        return mongoTemplate.remove<CollectionModel>(query)
    }

}