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

package io.opendmp.dataflow.api.controller

import io.opendmp.common.model.ProcessorType
import io.opendmp.common.model.SourceType
import io.opendmp.common.model.properties.DestinationType
import io.opendmp.dataflow.model.TriggerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/dataflow_api/lookup")
class LookupController {

    @GetMapping("/processor_types")
    fun getProcessorTypes(): Flow<ProcessorType> {
        return ProcessorType.values().asFlow()
    }

    @GetMapping("/trigger_types")
    fun getTriggerTypes(): Flow<TriggerType> {
        return TriggerType.values().asFlow()
    }

    @GetMapping("/source_types")
    fun getSourceTypes(@RequestParam(required = false) processorType: ProcessorType?) : Flow<SourceType> {
        val sourceTypes = SourceType.values().filter { it != SourceType.NONE }
        return when (processorType) {
            ProcessorType.INGEST ->
                sourceTypes.filter { it.toString().startsWith("INGEST_") }.asFlow()
            else -> sourceTypes.filter{ !it.toString().startsWith("INGEST") }.asFlow()
        }
    }

    @GetMapping("/destination_types")
    fun getDestinationTypes(): Flow<DestinationType> {
        return DestinationType.values().asFlow()
    }

}