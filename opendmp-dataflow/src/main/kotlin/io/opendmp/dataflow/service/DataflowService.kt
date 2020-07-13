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
import io.opendmp.dataflow.api.request.CreateDataflowRequest
import io.opendmp.dataflow.model.DataflowModel
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

@Service
class DataflowService (private val mongoTemplate: ReactiveMongoTemplate) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun createDataflow(data : CreateDataflowRequest,
                       authentication: Authentication) : Mono<DataflowModel> {

        val dataflow = DataflowModel(
                name = data.name!!,
                description = data.description,
                group = data.group,
                creator = "")
        return mongoTemplate.save<DataflowModel>(dataflow)
    }

    fun get(id: String) : Mono<DataflowModel> {
        return mongoTemplate.findById<DataflowModel>(id)
    }

    suspend fun getList() : Flow<DataflowModel> {
        return mongoTemplate.findAll<DataflowModel>().asFlow()
    }

    fun delete(id: String) : Mono<DeleteResult> {
        val query = Query(Criteria.where("id").isEqualTo(id))
        val result = mongoTemplate.remove<DataflowModel>(query)
        return result.doOnNext{
            // And now we delete all of the processors in this dataflow
            if(it.deletedCount > 0) {
                val query2 = Query(Criteria.where("flowId").isEqualTo(id))
                mongoTemplate.remove<ProcessorModel>(query2)
                log.info("Deleted dataflow $id and all associated processors")
            }
        }
    }

    fun getProcessors(id: String) : Flow<ProcessorModel> {
        val query =
                Query(Criteria.where("flowId").isEqualTo(id))
                        .with(Sort.by(Sort.Direction.ASC, "phase", "order"))
        return mongoTemplate.find<ProcessorModel>(query).asFlow()
    }

}