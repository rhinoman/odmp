package io.opendmp.processor.ingest

import io.opendmp.common.model.SourceType

data class IngestRequest(val sourceType: SourceType) {
}