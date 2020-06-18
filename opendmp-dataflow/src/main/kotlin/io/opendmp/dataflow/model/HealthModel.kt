package io.opendmp.dataflow.model

import java.time.LocalDateTime

class HealthModel(val state: HealthState = HealthState.OK,
                  val lastError: String? = null,
                  val lastErrorTime: LocalDateTime? = null) {
}