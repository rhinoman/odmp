package io.opendmp.dataflow.api.request

import javax.validation.constraints.NotBlank

data class CreateDataflowRequest(
        @field:NotBlank(message = "Name is required")
        val name: String,
        val description: String = "No description",
        val group: String = "No group"
) {}