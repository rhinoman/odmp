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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.opendmp.common.exception.CollectProcessorException
import io.opendmp.common.message.CollectionCompleteMessage
import io.opendmp.common.model.properties.DestinationType
import io.opendmp.common.model.ProcessorRunModel
import io.opendmp.common.model.Result
import io.opendmp.processor.config.SpringContext
import io.opendmp.processor.messaging.RunPlanStatusDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.apache.camel.CamelExecutionException
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.component.aws2.s3.AWS2S3Constants
import org.apache.camel.spring.SpringCamelContext
import org.apache.camel.spring.boot.SpringBootCamelContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowire
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Configurable
import java.time.Instant
import java.util.*

class CollectProcessor(processor: ProcessorRunModel) : AbstractProcessor(processor) {

    private val producerTemplate: ProducerTemplate =
            SpringContext.getBean(ProducerTemplate::class)
    private val runPlanStatusDispatcher: RunPlanStatusDispatcher =
            SpringContext.getBean(RunPlanStatusDispatcher::class)

    private val log = LoggerFactory.getLogger(javaClass)

    override fun process(exchange: Exchange?) {

        val props = processor.properties!!
        val destinationType = DestinationType.valueOf(props["type"].toString())
        val collectionId = props["collection"].toString()
        val prefix: String? = props["prefix"]?.toString()
        val payload = exchange?.getIn()?.getBody(ByteArray::class.java)
                ?: throw CollectProcessorException("No data to process")

        val time: Instant = Instant.now()
        val recordId = UUID.randomUUID().toString().replace("-", "")
        var endpoint: String = ""
        var location: String = ""

        var result: Result = Result.SUCCESS
        var error: String? = null
        try {
            when(destinationType) {
                DestinationType.FOLDER -> {
                    val folderLocation = props["location"].toString()
                    endpoint = "file://$folderLocation"
                    location = "$folderLocation/$recordId"
                    producerTemplate.sendBodyAndHeader(endpoint, payload, Exchange.FILE_NAME, recordId)
                }
                DestinationType.S3 -> {
                    val bucket = props["bucket"].toString()
                    val s3key = props["key"].toString()
                    location = "$bucket:$s3key/$recordId"
                    endpoint = "aws2-s3://$bucket"
                    producerTemplate.sendBodyAndHeader(endpoint, payload, AWS2S3Constants.KEY, "$s3key/$recordId")
                }
                else -> throw CollectProcessorException("Destination type $destinationType is unsupported")
            }
        } catch(cex: CamelExecutionException) {
            log.error("Error exporting data", cex)
            result = Result.ERROR
            error = "Error exporting data: ${cex.localizedMessage}"
        }

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
                errorMessage = error)


        runPlanStatusDispatcher.sendCollectionComplete(msg)

        exchange.getIn().body = payload
    }
}