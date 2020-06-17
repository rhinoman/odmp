package io.opendmp.dataflow.model

import java.util.*

class HealthModel(val state: HealthState = HealthState.OK,
                  val lastError: String? = null,
                  val lastErrorTime: Date? = null) {
}