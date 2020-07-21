package io.opendmp.common.message

import io.opendmp.common.model.ProcessorType

data class ProcessRequestMessage(
    val runPlanId: String,
    val processorType: ProcessorType) {
}