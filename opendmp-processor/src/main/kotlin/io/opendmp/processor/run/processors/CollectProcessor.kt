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

package io.opendmp.processor.run.processors

import io.opendmp.common.exception.CollectProcessorException
import io.opendmp.common.message.CollectionCompleteMessage
import io.opendmp.common.model.DataEvent
import io.opendmp.common.model.DataEventType
import io.opendmp.common.model.ProcessorRunModel
import io.opendmp.common.model.Result
import io.opendmp.common.model.properties.DestinationType
import io.opendmp.processor.config.SpringContext
import io.opendmp.processor.domain.DataEnvelope
import io.opendmp.processor.messaging.RunPlanStatusDispatcher
import org.apache.camel.CamelExecutionException
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.component.aws.s3.S3Constants
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class CollectProcessor(processor: ProcessorRunModel) : AbstractProcessor(processor) {

    private val producerTemplate: ProducerTemplate =
            SpringContext.getBean(ProducerTemplate::class)
    private val runPlanStatusDispatcher: RunPlanStatusDispatcher =
            SpringContext.getBean(RunPlanStatusDispatcher::class)

    private val log = LoggerFactory.getLogger(javaClass)

    private val fmt = DateTimeFormatter
            .ofPattern("yyyyDDDHHmmss.S")
            .withZone(ZoneId.systemDefault())

    override fun process(exchange: Exchange?) {

        val props = processor.properties!!
        val destinationType = DestinationType.valueOf(props["type"].toString())
        val collectionId = props["collection"].toString()
        val prefix: String? = props["prefix"]?.toString()

        val envelope = exchange?.getProperty("dataEnvelope") as DataEnvelope
        envelope.history.add(
                DataEvent(dataTag = envelope.tag,
                        eventType = DataEventType.COLLECTED,
                        processorId = processor.id,
                        processorName = processor.name,
                        description = "Exported data to $destinationType"))

        val time: Instant = Instant.now()
        val recordId = UUID.randomUUID().toString().replace("-", "")
        var location = ""

        var result: Result = Result.SUCCESS
        var error: String? = null
        try {
            when(destinationType) {
                DestinationType.FOLDER -> {
                    val folderLocation = props["location"].toString()
                    val endpoint = "file://$folderLocation"
                    location = "$folderLocation/$recordId"
                    producerTemplate.sendBodyAndHeader(endpoint,
                            exchange.getIn().body,
                            Exchange.FILE_NAME, recordId)
                }
                DestinationType.S3 -> {
                    val bucket = props["bucket"].toString()
                    val s3key = props["key"].toString()
                    val mimeType = props["mimeType"]?.toString() ?: "application/octet-stream"
                    location = "$bucket:$s3key/$recordId"
                    val endpoint = "aws-s3://$bucket"
                    val fname = "$prefix-${fmt.format(time)}"
                    val headers = mapOf(
                            S3Constants.KEY to "$s3key/$recordId",
                            S3Constants.CONTENT_TYPE to mimeType,
                            S3Constants.CONTENT_DISPOSITION to "attachment;filename=\"$fname\""
                    )
                    producerTemplate.sendBodyAndHeaders(endpoint,
                            exchange.getIn().body,
                            headers)
                }
                else -> throw CollectProcessorException("Destination type $destinationType is unsupported")
            }
        } catch(cex: CamelExecutionException) {
            log.error("Error exporting data", cex)
            result = Result.ERROR
            error = "Error exporting data: ${cex.localizedMessage}"
        }
        val history: List<List<DataEvent>> = listOf(envelope.history, envelope.paths.flatten())
        //Finally, send a message back to the dataflow service with collection information
        val msg = CollectionCompleteMessage(
                destinationType = destinationType,
                flowId = processor.flowId,
                processorId = processor.id,
                timeStamp = time,
                location = location,
                collectionId = collectionId,
                result = result,
                prefix = prefix,
                dataTag = envelope.tag,
                history = history,
                errorMessage = error)


        runPlanStatusDispatcher.sendCollectionComplete(msg)

        exchange.setProperty("dataEnvelope", envelope)
    }
}