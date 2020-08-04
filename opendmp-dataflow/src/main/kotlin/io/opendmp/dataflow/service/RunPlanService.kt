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

import io.opendmp.common.message.StopRunPlanRequestMessage
import io.opendmp.dataflow.messaging.RunPlanDispatcher
import io.opendmp.dataflow.model.DataflowModel
import io.opendmp.dataflow.model.ProcessorModel
import io.opendmp.dataflow.model.runplan.RunPlanModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

@Service
class RunPlanService(@Autowired private val mongoTemplate: ReactiveMongoTemplate,
                     @Autowired private val dispatcher: RunPlanDispatcher) {

    private val log = LoggerFactory.getLogger(RunPlanService::class.java)
    private val coroutineContext = Dispatchers.IO + SupervisorJob()

    suspend fun generateRunPlan(dataflow: DataflowModel) : RunPlanModel {
        log.debug("Loading Dataflow ${dataflow.name}")
        val pQ = Query(Criteria.where("flowId").isEqualTo(dataflow.id))
        val procs = mongoTemplate.find<ProcessorModel>(pQ).asFlow()
        return RunPlanModel.createRunPlan(dataflow, procs.toList())
    }

    suspend fun dispatchDataflow(dataflow: DataflowModel) {
        val runPlan = mongoTemplate.save(generateRunPlan(dataflow))
        log.info("Dispatching Dataflow ${dataflow.name}")
        runPlan.toFuture().thenAccept {
            dispatcher.dispatchRunPlan(it.createStartMessage())
        }
    }

    fun dispatchDataflow(dataflow: Mono<DataflowModel>) {
        dataflow.toFuture().thenAccept{
            CoroutineScope(coroutineContext).launch {
                dispatchDataflow(it)
            }
        }
    }

    suspend fun dispatchDataflows() {
        log.info("Loading enabled dataflows")
        val dfQ = Query(Criteria.where("enabled").isEqualTo(true))
        mongoTemplate.find<DataflowModel>(dfQ).asFlow().collect { df ->
            dispatchDataflow(df)
        }
    }

    fun stopDataflow(dataflowId: String) {
        val query = Query(Criteria.where("flowId").isEqualTo(dataflowId))
        mongoTemplate.findOne<RunPlanModel>(query).toFuture().thenAccept {
            dispatcher.stopRunPlan(StopRunPlanRequestMessage(UUID.randomUUID().toString(), it.id))
            //TODO: Move this to after an ack message that the route was successfully stopped
            mongoTemplate.findAndRemove<RunPlanModel>(Query(Criteria.where("flowId").isEqualTo(dataflowId)))
        }
    }


    /**
     * On application start, we want to immediately start loading and dispatching Dataflows
     */
    @EventListener
    fun onApplicationStart(event: ApplicationReadyEvent) {
        val scope = CoroutineScope(coroutineContext)
        scope.launch {
            //Make sure all the run models are cleared out
            mongoTemplate.findAllAndRemove<RunPlanModel>(Query())
            dispatchDataflows()
        }
    }
}