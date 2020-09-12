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

package io.opendmp.processor

import io.opendmp.common.model.*
import io.opendmp.common.model.properties.ScriptLanguage
import io.opendmp.processor.domain.DataEnvelope
import java.util.*

object TestUtils {

    fun createProcessor(name: String,
                        type: ProcessorType,
                        inputs: List<SourceModel>,
                        properties: Map<String, Any> = mapOf()) : ProcessorRunModel {
        return ProcessorRunModel(
                id = UUID.randomUUID().toString(),
                flowId = UUID.randomUUID().toString(),
                name = name,
                inputs = inputs,
                type = type,
                properties = properties
        )
    }

    fun createFileIngestProcessor(name: String) : ProcessorRunModel {
        return createProcessor(
                name,
                ProcessorType.INGEST,
                listOf(SourceModel(SourceType.INGEST_FILE_DROP, "/tmp/input")))
    }

    fun createScriptProcessor(name: String, inputProcs: List<String>, code: String) : ProcessorRunModel {
        return createProcessor(
                name,
                ProcessorType.SCRIPT,
                inputProcs.map{ SourceModel(SourceType.PROCESSOR, it)},
                properties = mapOf("language" to ScriptLanguage.CLOJURE.toString(), "code" to code)
        )
    }

    fun createDataEnvelope() : DataEnvelope {
        val tag = UUID.randomUUID().toString()
        return DataEnvelope(
                tag = tag,
                history = mutableListOf(DataEvent(
                        dataTag = tag,
                        eventType = DataEventType.INGESTED,
                        processorName = "INGESTER",
                        processorId = UUID.randomUUID().toString())))
    }
}