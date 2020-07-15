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

package io.opendmp.dataflow.api.request

import io.opendmp.dataflow.model.ProcessorType
import io.opendmp.dataflow.model.SourceModel
import io.opendmp.dataflow.model.SourceType
import io.opendmp.dataflow.model.TriggerType
import org.springframework.data.annotation.Reference
import org.springframework.data.mongodb.core.index.Indexed
import javax.validation.constraints.*

data class CreateProcessorRequest(
        @field:NotBlank(message = "Parent flow id is required")
        val flowId: String?,
        @field:NotBlank(message = "Name is required")
        @field:Size(min = 2, max = 32, message = "Name must be between 2 and 32 characters.")
        val name: String?,
        @field:Size(min = 2, max = 128, message = "Description must be between 2 and 128 characters.")
        val description: String? = "No description",
        @field:Min(1, message = "Phase number is required")
        val phase: Int?,
        @field:NotNull(message = "Processor Type is required")
        val type: ProcessorType?
) {
}