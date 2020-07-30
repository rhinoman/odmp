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

package io.opendmp.processor.run

import io.opendmp.common.exception.RunPlanLogicException
import io.opendmp.common.exception.UnsupportedProcessorTypeException
import io.opendmp.common.model.ProcessorRunModel
import io.opendmp.common.model.ProcessorType
import io.opendmp.common.model.SourceType
import io.opendmp.processor.domain.RunPlan
import org.apache.camel.builder.RouteBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate

class RunPlanRouteBuilder(private val runPlan: RunPlan): RouteBuilder() {

    override fun configure() {
        runPlan.startingProcessors.forEach { spid ->
            val sp = runPlan.processors[spid]
                    ?: error("Starting processor missing from processor map")

            startRoute(sp)
        }
    }

    private fun startRoute(sp: ProcessorRunModel) {
        // Here we assume (and will enforce) that a starting processor has only one input
        // Camel doesn't really support it and I can't imagine a use case for it.
        val source = sp.inputs.first()
        val sourceEp = when(sp.type) {
            ProcessorType.INGEST ->
                Utils.generateIngestEndpoint(source)
            else ->
                throw UnsupportedProcessorTypeException("The processor type ${sp.type} is not supported as a starting processor")
        }

        val deps: List<ProcessorRunModel?> =
                runPlan.processorDependencyMap[sp.id]?.map { runPlan.processors[it] } ?: listOf()
        when {
            deps.size == 1 -> {
                val dest = "seda:${deps.first()!!.id}"
                from(sourceEp).to(dest)
            }
            deps.size > 1 -> {
                val dest = deps.map { d -> "seda:${d!!.id}"}.joinToString(",")
                from(sourceEp)
                        .multicast()
                        .parallelProcessing()
                        .to(dest)
            }
            else -> {
                throw RunPlanLogicException("Starting processor has no outputs")
            }
        }
    }

    private fun continueRoute(curProc: ProcessorRunModel) {

    }

}