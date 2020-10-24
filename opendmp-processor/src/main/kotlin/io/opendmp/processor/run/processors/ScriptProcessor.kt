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

import io.opendmp.common.exception.ProcessorDefinitionException
import io.opendmp.common.exception.ScriptExecutionException
import io.opendmp.common.model.DataEvent
import io.opendmp.common.model.DataEventType
import io.opendmp.common.model.ProcessorRunModel
import io.opendmp.common.model.properties.ScriptLanguage
import io.opendmp.processor.domain.DataEnvelope
import io.opendmp.processor.executors.ClojureExecutor
import io.opendmp.processor.executors.PythonExecutor
import org.apache.camel.Exchange

class ScriptProcessor(processor: ProcessorRunModel) : AbstractProcessor(processor) {
    override fun process(exchange: Exchange?) {
        val props = processor.properties!!
        val language = ScriptLanguage.valueOf(props["language"].toString())
        val code = props["code"].toString()

        val envelope = exchange?.getProperty("dataEnvelope") as DataEnvelope

        val result = when(language) {
            ScriptLanguage.CLOJURE ->
                ClojureExecutor().executeScript(code, exchange.getIn().getBody(ByteArray::class.java))
            ScriptLanguage.PYTHON ->
                exchange.getIn().body
//                PythonExecutor().executeScript(code, exchange.getIn().getBody(ByteArray::class.java))
            else -> throw ScriptExecutionException("Script language $language is unsupported")
        }
        envelope.history.add(DataEvent(
                dataTag = envelope.tag,
                eventType = DataEventType.TRANSFORMED,
                processorId = processor.id,
                processorName = processor.name))

        exchange.setProperty("dataEnvelope", envelope)
        exchange.getIn().body = result
    }
}