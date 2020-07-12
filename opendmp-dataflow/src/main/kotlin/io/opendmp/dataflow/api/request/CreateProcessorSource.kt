package io.opendmp.dataflow.api.request

import io.opendmp.dataflow.model.SourceType
import javax.validation.constraints.Max

data class CreateProcessorSource(
    val sourceType: SourceType = SourceType.PROCESSOR,
    @field:Max(36)
    val sourceId: String?){}