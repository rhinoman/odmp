package io.opendmp.dataflow.model

import javax.validation.constraints.NotNull

data class SourceModel(
        val sourceType: SourceType = SourceType.PROCESSOR,
        val sourceId: String?) {}