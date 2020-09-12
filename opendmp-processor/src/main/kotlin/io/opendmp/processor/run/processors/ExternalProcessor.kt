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

import io.opendmp.common.exception.ProcessorDefinitionException
import io.opendmp.common.model.DataEvent
import io.opendmp.common.model.DataEventType
import io.opendmp.common.model.ProcessorRunModel
import io.opendmp.processor.domain.DataEnvelope
import org.apache.camel.Exchange
import java.util.concurrent.TimeUnit

class ExternalProcessor(processor: ProcessorRunModel) : AbstractProcessor(processor) {
    override fun process(exchange: Exchange?) {
        val props = processor.properties!!
        val command = props["command"].toString()

        val envelope = exchange?.getIn()?.getBody(DataEnvelope::class.java)
                ?: throw ProcessorDefinitionException("Data Envelope not found")


        val proc = ProcessBuilder(command)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()
        proc.waitFor(60, TimeUnit.MINUTES)
        proc.outputStream.write(envelope.data)
        envelope.data = proc.inputStream.readBytes()
        envelope.history.add(DataEvent(
                dataTag = envelope.tag,
                eventType = DataEventType.TRANSFORMED,
                processorId = processor.id,
                processorName = processor.name,
                description = "Executed Command: $command"))

        exchange.getIn().body = envelope
    }
}