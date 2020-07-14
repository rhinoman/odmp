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
import io.opendmp.dataflow.api.request.CreateProcessorRequest
import io.opendmp.dataflow.model.ProcessorModel
import io.opendmp.dataflow.model.SourceModel
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.remove
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ProcessorService (private val mongoTemplate: ReactiveMongoTemplate) {

    fun createProcessor(data : CreateProcessorRequest,
                        authentication: Authentication) : Mono<ProcessorModel> {

        val username = when(val principal = authentication.principal) {
            is OidcUser -> principal.preferredUsername
            else -> "test_user"
        }
        val inputs: List<SourceModel> = data.inputs?.map {
            SourceModel(
                    sourceType = it.sourceType,
                    sourceId = it.sourceId
            )} ?: mutableListOf()

        val processor = ProcessorModel(
                flowId = data.flowId!!,
                name = data.name!!,
                description = data.description,
                phase = data.phase!!,
                order = data.order!!,
                type = data.type!!,
                inputs = inputs,
                creator = username
        )

        return mongoTemplate.save<ProcessorModel>(processor)
    }

    fun get(id: String) : Mono<ProcessorModel> {
        return mongoTemplate.findById(id)
    }

    fun deleteProcessor(id: String) : Mono<DeleteResult> {
        val query = Query(Criteria.where("id").isEqualTo(id))
        return mongoTemplate.remove<ProcessorModel>(query)
    }

}