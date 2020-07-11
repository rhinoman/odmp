package io.opendmp.dataflow.api.request

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import kotlin.math.min

data class CreateDataflowRequest(
        @field:NotBlank(message = "Name is required")
        @field:Size(min = 3, max = 64, message = "Name must be between 3 and 64 characters")
        val name: String?,
        @field:Size(min = 1, max = 256, message = "Description has a max length of 256 characters")
        val description: String? = "No description",
        val group: String? = "No group"
) {}