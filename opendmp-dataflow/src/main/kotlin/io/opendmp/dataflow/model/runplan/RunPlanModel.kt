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

package io.opendmp.dataflow.model.runplan

import io.opendmp.common.message.StartRunPlanRequestMessage
import io.opendmp.common.model.*
import io.opendmp.dataflow.model.DataflowModel
import io.opendmp.dataflow.model.ProcessorModel
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.*

@Document(collection = "run_plans")
data class RunPlanModel(@Id val id: String = ObjectId.get().toHexString(),
                        @Indexed(name = "run_plan_flow_id_index", background = true)
                        val flowId: String,
                        // The id of the starting (e.g., ingest) processors
                        // These processors are not dependent on any other processors
                        // and can thus start right away
                        val startingProcessors: List<String>,
                        // A map of dependency relationships amongst processors
                        val processorDependencyMap: Map<String, List<String>>,
                        // The processors
                        val processors: Map<String, ProcessorModel>,
                        var runState: RunState = RunState.IDLE,
                        var errors: MutableList<String> = mutableListOf(),
                        @CreatedDate
                        val createdOn: Instant = Instant.now(),
                        @LastModifiedDate
                        var updatedOn: Instant = Instant.now(),
                        var finishedDate: Instant? = null) {

    fun createStartMessage() = StartRunPlanRequestMessage(
            requestId = this.id,
            flowId = this.flowId,
            startingProcessors = this.startingProcessors,
            processorDependencyMap = this.processorDependencyMap,
            processors = this.processors.map{it.key to it.value.toRunModel()}.toMap()
    )

    companion object {
        fun createRunPlan(dataflow: DataflowModel, processors: List<ProcessorModel>) : RunPlanModel {
            //Find the starting processors
            val startingProcessors = processors.filter {
                it.inputs.none { ip -> ip.sourceType == SourceType.PROCESSOR }
            }.map{it.id}

            val depMap: MutableMap<String, MutableList<String>> = mutableMapOf()
            val procMap: MutableMap<String, ProcessorModel> = mutableMapOf()

            //Build the dependency map
            processors.forEach { pr ->
                pr.inputs.filter { ip -> ip.sourceType == SourceType.PROCESSOR }.map{ sr ->
                    if(depMap[sr.sourceLocation] == null) {
                        depMap[sr.sourceLocation!!] = mutableListOf(pr.id)
                    } else {
                        depMap[sr.sourceLocation]?.add(pr.id)
                    }
                }
                procMap[pr.id] = pr
            }

            return RunPlanModel(
                    flowId = dataflow.id,
                    startingProcessors = startingProcessors,
                    processorDependencyMap = depMap,
                    processors = procMap
            )
        }
    }

}