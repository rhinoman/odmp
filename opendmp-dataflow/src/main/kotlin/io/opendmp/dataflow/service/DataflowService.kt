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
import io.opendmp.common.model.HealthModel
import io.opendmp.common.model.HealthState
import io.opendmp.common.model.RunState
import io.opendmp.dataflow.Util
import io.opendmp.dataflow.api.exception.NotFoundException
import io.opendmp.dataflow.api.request.CreateDataflowRequest
import io.opendmp.dataflow.api.request.UpdateDataflowRequest
import io.opendmp.dataflow.api.response.DataflowListItem
import io.opendmp.dataflow.model.DataflowModel
import io.opendmp.dataflow.model.ProcessorModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import io.opendmp.dataflow.api.exception.BadRequestException
import io.opendmp.dataflow.api.exception.ResourceConflictException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.LocalDateTime
import java.time.ZoneId
import javax.xml.crypto.Data

@Service
class DataflowService (private val mongoTemplate: ReactiveMongoTemplate,
                       private val runPlanService: RunPlanService) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val coroutineContext = Dispatchers.IO

    fun createDataflow(data : CreateDataflowRequest,
                       authentication: Authentication) : Mono<DataflowModel> {

        val username = Util.getUsername(authentication)

        val dataflow = DataflowModel(
                name = data.name!!,
                description = data.description,
                group = data.group,
                creator = username)
        return mongoTemplate.save<DataflowModel>(dataflow)
    }

    fun get(id: String) : Mono<DataflowModel> {
        return mongoTemplate.findById<DataflowModel>(id)
    }

    fun updateDataflow(id: String,
                       data: UpdateDataflowRequest,
                       authentication: Authentication) : Mono<DataflowModel> {

        //val username = Util.getUsername(authentication)
        return mongoTemplate.findById<DataflowModel>(id).flatMap { cur ->
            val oldEnableState = cur.enabled
            cur.name = data.name!!
            cur.description = data.description
            cur.group = data.group
            cur.enabled = data.enabled!!
            val updatedDataflow = mongoTemplate.save(cur)
            if(cur.enabled && !oldEnableState) {
                runPlanService.dispatchDataflow(updatedDataflow)
            } else if(!cur.enabled && oldEnableState) {
                log.info("Stopping dataflow ${cur.name}")
                runPlanService.stopDataflow(cur.id)
            }
            updatedDataflow
        }
    }

    suspend fun getList(enabled: Boolean? = null) : Flow<DataflowListItem> {
        val query = Query()
        if(enabled != null) {
            query.addCriteria(Criteria.where("enabled").isEqualTo(enabled))
        }
        val dataflows = mongoTemplate.find<DataflowModel>(query)
        return dataflows.asFlow().map { df ->
            val runPlan = runPlanService.getForDataflow(df.id).asFlow().firstOrNull()
            val health = if(runPlan?.errors != null && runPlan.errors.isNotEmpty()) {
                val lastError = runPlan.errors.values.maxBy { it.time }!!
                HealthModel(
                        state = HealthState.ERROR,
                        lastError = lastError.errorMessage,
                        lastErrorTime = LocalDateTime.ofInstant(lastError.time, ZoneId.systemDefault()))
            } else {
                HealthModel(state = HealthState.OK)
            }
            DataflowListItem(
                    dataflow = df,
                    state = if(df.enabled) RunState.RUNNING else RunState.DISABLED,
                    health = health)
        }
    }

    fun delete(id: String) : Mono<DeleteResult> {
        val query = Query(Criteria.where("id").isEqualTo(id))
        return mongoTemplate.findById<DataflowModel>(id)
                .switchIfEmpty { throw NotFoundException() }
                .flatMap { df ->
                    if(df.enabled) {
                        Mono.error(ResourceConflictException("Dataflow cannot be deleted while running!"))
                    } else {
                        log.info("Deleted dataflow $id and all associated processors")
                        mongoTemplate.remove(df)
                    }
                }.flatMap {
                    // And now we delete all of the processors in this dataflow
                    val query2 = Query(Criteria.where("flowId").isEqualTo(id))
                    mongoTemplate.remove<ProcessorModel>(query2)
                }
    }

    fun getProcessors(id: String, phase: Int?) : Flow<ProcessorModel> {
        val query = Query(Criteria.where("flowId").isEqualTo(id))
                .with(Sort.by(Sort.Direction.ASC, "phase", "order"))
        if(phase != null) {
            query.addCriteria(Criteria.where("phase").isEqualTo(id))
        }
        return mongoTemplate.find<ProcessorModel>(query).asFlow()
    }

}