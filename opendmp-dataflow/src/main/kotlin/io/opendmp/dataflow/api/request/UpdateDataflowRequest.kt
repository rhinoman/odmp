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

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class UpdateDataflowRequest(
        @field:NotBlank(message = "Name is required")
        @field:Size(min = 3, max = 64, message = "Name must be between 3 and 64 characters")
        val name: String?,
        @field:Size(min = 1, max = 256, message = "Description has a maximum of 256 characters")
        val description: String? = "No description",
        val group: String? = "",
        @field:NotNull(message = "Enabled must be set")
        val enabled: Boolean? = false
) {
}