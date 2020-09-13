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
import kotlinx.coroutines.*
import org.apache.camel.CamelExecutionException
import org.apache.camel.Exchange
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.charset.Charset
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class ExternalProcessor(processor: ProcessorRunModel) : AbstractProcessor(processor) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val coroutineContext = Dispatchers.IO + SupervisorJob()

    suspend fun streamGobble(bis: BufferedInputStream): ByteArray {
        val bytes = bis.readAllBytes()
        bis.close()
        return bytes
    }

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

        // Write the data to stdin async
        CoroutineScope(coroutineContext).launch {
            val outputStream = BufferedOutputStream(proc.outputStream)
            outputStream.write(exchange.getIn().getBody(ByteArray::class.java))
            outputStream.close()
        }
        // Careful of these two
        val inputBytes = CoroutineScope(coroutineContext).async {
            streamGobble(BufferedInputStream(proc.inputStream))
        }
        val errorBytes = CoroutineScope(coroutineContext).async {
            streamGobble(BufferedInputStream(proc.errorStream))
        }
        //Get the data from the pipe
        //Wait up to timeoutSeconds for process to complete
        log.info("Waiting up to $timeoutSeconds seconds for $command to finish")
        proc.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        runBlocking {
            if (proc.exitValue() != 0) {
                val err = errorBytes.await().decodeToString()
                val msg = "Error occurred executing $command: $err"
                throw CommandExecutionException(msg)
            } else {
                exchange.getIn().body = inputBytes.await()
                envelope.history.add(DataEvent(
                        dataTag = envelope.tag,
                        eventType = DataEventType.TRANSFORMED,
                        processorId = processor.id,
                        processorName = processor.name,
                        description = "Executed Command: $command"))

                exchange.setProperty("dataEnvelope", envelope)
            }
        }
    }
}