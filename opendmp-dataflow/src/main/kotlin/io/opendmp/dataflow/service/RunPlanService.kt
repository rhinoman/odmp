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
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Service

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
        val runPlan = generateRunPlan(dataflow)
        log.info("Dispatching Dataflow ${dataflow.name}")
        dispatcher.dispatchRunPlan(runPlan.createStartMessage())
    }

    suspend fun dispatchDataflows() {
        log.info("Loading enabled dataflows")
        val dfQ = Query(Criteria.where("enabled").isEqualTo(true))
        mongoTemplate.find<DataflowModel>(dfQ).asFlow().collect { df ->
            dispatchDataflow(df)
        }
    }


    /**
     * On application start, we want to immediately start loading and dispatching Dataflows
     */
    @EventListener
    fun onApplicationStart(event: ApplicationReadyEvent) {
        val scope = CoroutineScope(coroutineContext)
        scope.launch {
            dispatchDataflows()
        }
    }
}