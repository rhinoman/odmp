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

package io.opendmp.processor.run

import io.opendmp.common.exception.RunPlanLogicException
import io.opendmp.common.exception.UnsupportedProcessorTypeException
import io.opendmp.common.model.ProcessorRunModel
import io.opendmp.common.model.ProcessorType
import io.opendmp.processor.domain.RunPlan
import io.opendmp.processor.run.processors.CollectProcessor
import io.opendmp.processor.run.processors.CompletionProcessor
import io.opendmp.processor.run.processors.ScriptProcessor
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder

/**
 * RunPlanRouteBuilder - takes a Run Plan and builds camel routes to set up a data pipeline
 * This is kind of the heart of the whole system right here
 */
class RunPlanRouteBuilder(private val runPlan: RunPlan,
                          private val numRetries: Int = 5): RouteBuilder() {

    override fun configure() {
        //Set up error handling
        errorHandler(deadLetterChannel("direct:dead")
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .maximumRedeliveries(numRetries)
                .backOffMultiplier(2.0)
                .useExponentialBackOff())

        from("direct:dead")
                .setHeader("runPlan", constant(runPlan.id))
                .bean(FailureHandler(), "processFailure")
                .to("log:io.opendmp.processor.run?level=ERROR")

        //Build the routes
        runPlan.startingProcessors.forEach { spid ->
            val sp = runPlan.processors[spid]
                    ?: error("Starting processor missing from processor map")

            startRoute(sp)
        }
    }

    /**
     * startRoute - starts a chain of processors
     * Starting processors are not dependent on other processors and can start running right away
     */
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
        val routeId = "${runPlan.id}-${sp.id}"
        when {
            deps.size == 1 -> {
                val dest = "direct:${runPlan.id}-${deps.first()!!.id}"
                from(sourceEp)
                        .routeId(routeId)
                        .startupOrder(Utils.getNextStartupOrder())
                        .to(dest)
            }
            deps.size > 1 -> {
                // If we have more than 1 processor expecting output from this processor,
                // do a multicast
                val dest = deps.map { d -> "direct:${runPlan.id}-${d!!.id}"}

                from(sourceEp)
                        // Set a routeId to make finding this route in the Camel Context easier later
                        .routeId(routeId)
                        .startupOrder(Utils.getNextStartupOrder())
                        .multicast()
                        .parallelProcessing()
                        .to(*dest.toTypedArray())
            }
            else -> {
                throw RunPlanLogicException("Starting processor has no outputs")
            }
        }
        // Continue building with the dependencies
        deps.forEach { this.continueRoute(it!!) }
    }

    /**
     * continueRoute - for the processors from the middle of a chain to the end
     */
    private fun continueRoute(curProc: ProcessorRunModel) {
        val sourceEp = "direct:${runPlan.id}-${curProc.id}"
        val deps: List<ProcessorRunModel?> =
                runPlan.processorDependencyMap[curProc.id]?.map { runPlan.processors[it] } ?: listOf()
        val proc = when(curProc.type){
            ProcessorType.SCRIPT -> ScriptProcessor(curProc)
            ProcessorType.COLLECT -> CollectProcessor(curProc)
            else -> throw UnsupportedProcessorTypeException("The processor type ${curProc.type} is not supported")
        }
        val routeId = "${runPlan.id}-${curProc.id}"
        when {
            deps.size == 1 ->
                from(sourceEp)
                        .routeId(routeId)
                        .startupOrder(Utils.getNextStartupOrder())
                        .setHeader("processor", constant(curProc.id))
                        .process(proc).id(curProc.id)
                        .to("direct:${runPlan.id}-${deps.first()!!.id}").end()
            deps.size > 1 -> {
                val dest = deps.map { d -> "direct:${runPlan.id}-${d!!.id}"}
                from(sourceEp)
                        .routeId(routeId)
                        .startupOrder(Utils.getNextStartupOrder())
                        .setHeader("processor", constant(curProc.id))
                        .process(proc).id(curProc.id)
                        .multicast()
                        .parallelProcessing()
                        .to(*dest.toTypedArray())
            }
            else -> { //End of the line
                val completionId = "${runPlan.id}-${curProc.id}-complete"
                from(sourceEp)
                        .routeId(routeId)
                        .startupOrder(Utils.getNextStartupOrder())
                        .setHeader("processor", constant(curProc.id))
                        .process(proc).id(curProc.id)
                        .process(CompletionProcessor())
                        .id(completionId)
                        .end()
            }
        }
        deps.forEach { continueRoute(it!!) }
    }

}