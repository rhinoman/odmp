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
import io.opendmp.dataflow.api.exception.BadRequestException
import io.opendmp.dataflow.api.exception.NotFoundException
import io.opendmp.dataflow.api.request.CreateProcessorRequest
import io.opendmp.dataflow.api.request.UpdateProcessorRequest
import io.opendmp.dataflow.api.response.ProcessorDetail
import io.opendmp.dataflow.model.DataflowModel
import io.opendmp.dataflow.model.ProcessorModel
import org.springframework.data.mongodb.core.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import java.time.Instant

@Service
class ProcessorService (private val mongoTemplate: ReactiveMongoTemplate) {

    private fun getNumProcessorsInPhase(flowId: String, phase: Int) : Mono<Long> {
        val query = Query(Criteria
                .where("flowId").isEqualTo(flowId)
                .and("phase").isEqualTo(phase))
        return mongoTemplate.count<ProcessorModel>(query)
    }

    /**
     * Creates a new processor
     */
    fun createProcessor(data: CreateProcessorRequest,
                        authentication: Authentication) : Mono<ProcessorModel> {

        val username = when(val principal = authentication.principal) {
            is OidcUser -> principal.preferredUsername
            else -> "test_user"
        }
        val numProcs = getNumProcessorsInPhase(data.flowId!!, data.phase!!)

        return numProcs.flatMap {
            mongoTemplate.save(
                    ProcessorModel(
                            flowId = data.flowId,
                            name = data.name!!,
                            description = data.description,
                            phase = data.phase,
                            order = it.toInt() + 1,
                            type = data.type!!,
                            creator = username)
            )
        }
    }

    private fun validateProcsesorUpdate(proc: ProcessorModel,
                                        data: UpdateProcessorRequest) : Mono<ProcessorModel> {
        return mongoTemplate.findById<DataflowModel>(proc.flowId)
                .switchIfEmpty { throw NotFoundException() }
                .handle {df, sink ->
                    if(df.enabled) {
                        sink.error(BadRequestException("Dataflow is still enabled!"))
                    } else if (data.properties == null){
                        sink.error(BadRequestException("Properties are invalid!"))
                    } else {
                        proc.name = data.name!!
                        proc.description = data.description
                        proc.phase = data.phase!!
                        proc.order = data.order!!
                        proc.triggerType = data.triggerType!!
                        proc.type = data.type!!
                        proc.properties = data.properties.toMutableMap()
                        proc.inputs = data.inputs!!
                        proc.enabled = data.enabled!!
                        sink.next(proc)
                    }
                }
    }

    /**
     * Updates a Processor
     */
    fun updateProcessor(id: String,
                        data: UpdateProcessorRequest,
                        authentication: Authentication) : Mono<ProcessorModel> {
        return get(id)
                .switchIfEmpty { throw NotFoundException() }
                .map { validateProcsesorUpdate(it, data) }
                .flatMap{ mongoTemplate.save(it) }
    }

    fun get(id: String) : Mono<ProcessorModel> {
        return mongoTemplate.findById(id)
    }

    fun getDetail(id: String) : Mono<ProcessorDetail> {
        return get(id).flatMap { proc ->
            mongoTemplate.findById<DataflowModel>(proc.flowId).flatMap<ProcessorDetail> { df ->
                Mono.just(ProcessorDetail(processor = proc, dataflow = df))
            }
        }
    }

    /**
     * Delete a processor
     */
    fun deleteProcessor(id: String) : Mono<DeleteResult> {
        val query = Query(Criteria.where("id").isEqualTo(id))

        return getDetail(id).switchIfEmpty{ throw NotFoundException() }
                .handle{ d, sink ->
            if(d.dataflow.enabled) {
                sink.error(BadRequestException("Dataflow is enabled!"))
            } else {
                sink.next(mongoTemplate.remove<ProcessorModel>(query).block()!!)
            }
        }
    }

}