package io.opendmp.dataflow.api.request

import io.opendmp.dataflow.model.ProcessorType
import org.springframework.data.annotation.Reference
import org.springframework.data.mongodb.core.index.Indexed
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class CreateProcessorRequest(
        @field:NotBlank(message = "Parent flow id is required")
        val flowId: String,
        @field:NotBlank(message = "Name is required")
        val name: String,
        val description: String = "No description",
        @field:Min(1, message = "Phase number is required")
        val phase: Int,
        @field:Min(1, message = "Order is required")
        val order: Int,
        @field:NotNull(message = "Processor Type is required")
        val type: ProcessorType
) {
}