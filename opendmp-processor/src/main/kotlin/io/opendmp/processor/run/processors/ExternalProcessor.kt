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

import io.opendmp.common.exception.CommandExecutionException
import io.opendmp.common.exception.ProcessorDefinitionException
import io.opendmp.common.model.DataEvent
import io.opendmp.common.model.DataEventType
import io.opendmp.common.model.ProcessorRunModel
import io.opendmp.processor.domain.DataEnvelope
import org.apache.camel.CamelExecutionException
import org.apache.camel.Exchange
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class ExternalProcessor(processor: ProcessorRunModel) : AbstractProcessor(processor) {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun process(exchange: Exchange?) {
        val props = processor.properties!!
        val command = props["command"].toString()
        val timeoutSeconds =
                if(props.containsKey("timeout")) {
                    props["timeout"].toString().toLong()
                } else {
                    10L
                }
        val envelope: DataEnvelope = exchange?.getProperty("dataEnvelope") as DataEnvelope
        log.info("Running $command")
        val proc = ProcessBuilder(command.split(" ")).start()

        val bos = BufferedOutputStream(proc.outputStream)
        // Write the data to stdin in a separate thread
        thread {
            bos.write(exchange.getIn().getBody(ByteArray::class.java))
            bos.close()
        }
        println(proc.errorStream.available())

        //Get the data from the pipe
        //Wait up to timeoutSeconds for process to complete
        //log.info("Waiting up to $timeoutSeconds seconds for $command to finish")
        val bis = BufferedInputStream(proc.inputStream)
        exchange.getIn().body = bis.readAllBytes()
        proc.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        bis.close()
        envelope.history.add(DataEvent(
                dataTag = envelope.tag,
                eventType = DataEventType.TRANSFORMED,
                processorId = processor.id,
                processorName = processor.name,
                description = "Executed Command: $command"))

        exchange.setProperty("dataEnvelope", envelope)
    }
}