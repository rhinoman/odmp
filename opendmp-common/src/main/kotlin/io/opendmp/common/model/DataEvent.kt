/*
 * Copyright (c) 2020. The Open Data Management Platform contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opendmp.common.model

import java.time.Instant

data class DataEvent(
        val dataTag: String,
        val timestamp: Instant = Instant.now(),
        val eventType: DataEventType,
        val processorId: String,
        val processorName: String,
        val description: String? = null) {

    fun copy(tag: String): DataEvent {
        return DataEvent(
                dataTag = tag,
                eventType = this.eventType,
                processorId = this.processorId,
                processorName  = this.processorName,
                timestamp = this.timestamp,
                description = this.description)
    }

}