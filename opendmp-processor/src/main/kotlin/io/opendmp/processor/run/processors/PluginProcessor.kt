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

package io.opendmp.processor.run.processors

import io.opendmp.common.model.DataEvent
import io.opendmp.common.model.DataEventType
import io.opendmp.common.model.ProcessorRunModel
import io.opendmp.processor.config.SpringContext
import io.opendmp.processor.domain.DataEnvelope
import org.apache.camel.*
import org.slf4j.LoggerFactory


class PluginProcessor(processor: ProcessorRunModel) : AbstractProcessor(processor) {

    private val producerTemplate: FluentProducerTemplate =
            SpringContext.getBean(FluentProducerTemplate::class)

    private val camelContext: CamelContext =
            SpringContext.getBean(CamelContext::class)

    private val log = LoggerFactory.getLogger(javaClass)

    override fun process(exchange: Exchange?) {
        val props = processor.properties!!
        val serviceName = props["serviceName"].toString()
        val envelope = exchange?.getProperty("dataEnvelope") as DataEnvelope

        envelope.history.add(
                DataEvent(dataTag = envelope.tag,
                        eventType = DataEventType.TRANSFORMED,
                        processorId = processor.id,
                        processorName = processor.name,
                        description = "Processed with $serviceName"))

        exchange.getIn().setHeader("serviceCall", serviceName)
        exchange.setProperty("dataEnvelope", envelope)
    }
}