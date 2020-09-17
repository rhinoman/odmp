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

import com.amazonaws.client.builder.AwsClientBuilder
import io.opendmp.common.exception.*
import io.opendmp.common.model.ProcessorRunModel
import io.opendmp.common.model.ProcessorType
import io.opendmp.common.model.SourceModel
import io.opendmp.common.model.SourceType
import io.opendmp.processor.config.SpringContext
import io.opendmp.processor.domain.RunPlan
import io.opendmp.processor.run.processors.*
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.DeadLetterChannelBuilder
import org.apache.camel.builder.DefaultErrorHandlerBuilder
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.aws.s3.S3Constants
import org.apache.camel.model.RouteDefinition

/**
 * RunPlanRouteBuilder - takes a Run Plan and builds camel routes to set up a data pipeline
 * This is kind of the heart of the whole system right here
 */
class RunPlanRouteBuilder(private val runPlan: RunPlan,
                          private val numRetries: Int = 2): RouteBuilder() {

    private fun generateIngestEndpoint(source: SourceModel) : RouteDefinition {
        return when(source.sourceType) {
            SourceType.INGEST_FILE_DROP ->
                from("file://${source.sourceLocation!!}?readLock=changed")
            SourceType.INGEST_FTP ->
                from("ftp://${source.sourceLocation!!}?binary=true")
            SourceType.INGEST_S3 -> {
                val bucket = source.additionalProperties?.get("bucket")
                        ?: throw ProcessorDefinitionException("Bucket must be specified for S3 ingest")
                val keyPrefix = source.sourceLocation
                        ?: throw ProcessorDefinitionException("No S3 key prefix provided!")
                from("aws-s3://$bucket?prefix=$keyPrefix")
                        .setHeader(S3Constants.CONTENT_TYPE, constant("application/octet-stream"))
            }
            else -> throw NotImplementedException("SourceType ${source.sourceType} not supported")
        }

    }

    override fun configure() {
        //Set up error handling
        errorHandler(deadLetterChannel("direct:${runPlan.id}-dead")
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .allowRedeliveryWhileStopping(false)
                .maximumRedeliveries(numRetries)
                .redeliveryDelay(1000L))

        from("direct:${runPlan.id}-dead")
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

    fun getErrorHandlerForType(ptype: ProcessorType) : DefaultErrorHandlerBuilder {
        val deadLetterUri = "direct:${runPlan.id}-dead"
        return when(ptype) {
            ProcessorType.COLLECT ->
                deadLetterChannel(deadLetterUri)
                        .maximumRedeliveries(2)
                        .allowRedeliveryWhileStopping(false)
                        .redeliveryDelay(1500L)
            else ->
                deadLetterChannel(deadLetterUri)
                        .maximumRedeliveries(0)
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
                generateIngestEndpoint(source)
            else ->
                throw UnsupportedProcessorTypeException("The processor type ${sp.type} is not supported as a starting processor")
        }

        val deps: List<ProcessorRunModel?> =
                runPlan.processorDependencyMap[sp.id]?.map { runPlan.processors[it] } ?: listOf()
        val routeId = "${runPlan.id}-${sp.id}"
        var multi = false
        when {
            deps.size == 1 -> {
                val dest = "direct:${runPlan.id}-${deps.first()!!.id}"
                sourceEp
                        .convertBodyTo(ByteArray::class.java)
                        .routeId(routeId)
                        .startupOrder(Utils.getNextStartupOrder())
                        .process(DataWrapper(sp))
                        .to(dest)
            }
            deps.size > 1 -> {
                // If we have more than 1 processor expecting output from this processor,
                // do a multicast
                val dest = deps.map { d -> "seda:${runPlan.id}-${d!!.id}"}
                multi = true
                sourceEp
                        // Set a routeId to make finding this route in the Camel Context easier later
                        .routeId(routeId)
                        .convertBodyTo(ByteArray::class.java)
                        .startupOrder(Utils.getNextStartupOrder())
                        .process(DataWrapper(sp))
                        .multicast().onPrepare(MultiPrepareProcessor())
                        .parallelProcessing()
                        .to(*dest.toTypedArray())
            }
            else -> {
                throw RunPlanLogicException("Starting processor has no outputs")
            }
        }
        // Continue building with the dependencies
        deps.forEach { this.continueRoute(it!!, multi) }
    }

    /**
     * continueRoute - for the processors from the middle of a chain to the end
     * @param curProc The processor to execute
     * @param multi - If the source is from a multicast or single (direct)
     */
    private fun continueRoute(curProc: ProcessorRunModel, multi: Boolean = false) {
        val sourceEp = if(multi) {
            "seda:${runPlan.id}-${curProc.id}"
        } else {
            "direct:${runPlan.id}-${curProc.id}"
        }
        val deps: List<ProcessorRunModel?> =
                runPlan.processorDependencyMap[curProc.id]?.map { runPlan.processors[it] } ?: listOf()
        val proc = when(curProc.type){
            ProcessorType.SCRIPT -> ScriptProcessor(curProc)
            ProcessorType.EXTERNAL -> ExternalProcessor(curProc)
            ProcessorType.COLLECT -> CollectProcessor(curProc)
            else -> throw UnsupportedProcessorTypeException("The processor type ${curProc.type} is not supported")
        }
        val routeId = "${runPlan.id}-${curProc.id}"
        var nextMulti = false
        when {
            deps.size == 1 ->
                from(sourceEp)
                        .routeId(routeId)
                        .errorHandler(getErrorHandlerForType(curProc.type))
                        .startupOrder(Utils.getNextStartupOrder())
                        .setHeader("processor", constant(curProc.id))
                        .process(proc).id(curProc.id)
                        .to("direct:${runPlan.id}-${deps.first()!!.id}").end()
            deps.size > 1 -> {
                val dest = deps.map { d -> "seda:${runPlan.id}-${d!!.id}"}
                nextMulti = true
                from(sourceEp)
                        .routeId(routeId)
                        .errorHandler(getErrorHandlerForType(curProc.type))
                        .startupOrder(Utils.getNextStartupOrder())
                        .setHeader("processor", constant(curProc.id))
                        .process(proc).id(curProc.id)
                        .multicast().onPrepare(MultiPrepareProcessor())
                        .parallelProcessing()
                        .to(*dest.toTypedArray())
            }
            else -> { //End of the line
                val completionId = "${runPlan.id}-${curProc.id}-complete"
                from(sourceEp)
                        .routeId(routeId)
                        .errorHandler(getErrorHandlerForType(curProc.type))
                        .startupOrder(Utils.getNextStartupOrder())
                        .setHeader("processor", constant(curProc.id))
                        .process(proc).id(curProc.id)
                        .process(CompletionProcessor())
                        .id(completionId)
                        .end()
            }
        }
        deps.forEach { continueRoute(it!!, nextMulti) }
    }

}