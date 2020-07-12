package io.opendmp.dataflow.api.request

import io.opendmp.dataflow.model.ProcessorType
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
        @field:Min(1, message = "Order is required")
        val order: Int?,
        @field:NotNull(message = "Processor Type is required")
        val type: ProcessorType?,
        val triggerType: TriggerType? = TriggerType.AUTOMATIC,
        val sourceType: SourceType? = SourceType.PROCESSOR,
        @field:Size(max = 36)
        val sourceId: String? = ""
) {
}