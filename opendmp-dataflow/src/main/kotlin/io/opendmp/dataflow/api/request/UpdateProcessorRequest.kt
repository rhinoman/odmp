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

import io.opendmp.common.model.ProcessorType
import io.opendmp.common.model.SourceModel
import io.opendmp.dataflow.model.TriggerType
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class UpdateProcessorRequest(
        @field:Size(min = 2, max = 32, message = "Name must be between 2 and 32 characters")
        val name: String?,
        @field:Size(max=128, message = "Description must be less than 128 characters")
        val description: String? = "No description",
        @field:Min(1, message = "Phase number is not valid")
        val phase: Int?,
        @field:Min(1, message = "Order is required")
        val order: Int?,
        @field:NotNull(message = "Trigger Type is required")
        val triggerType: TriggerType?,
        @field:NotNull(message = "Processor Type is required")
        val type: ProcessorType?,
        val properties: Map<String, Any>? = mutableMapOf(),
        val inputs: List<SourceModel>? = mutableListOf(),
        val enabled: Boolean? = false
) {}