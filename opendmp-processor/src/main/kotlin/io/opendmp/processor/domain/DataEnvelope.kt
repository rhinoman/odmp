/*
 * Copyright (c) 2020. James Adam and the Open Data Management Platform contributors.
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

package io.opendmp.processor.domain

import io.opendmp.common.model.DataEvent
import java.util.*

/**
 * Wraps data as it flows through the system
 * @property tag A tag is assigned to data as soon as it is ingested
 * @property history History is logged by each processor in the flow
 * @property paths During an aggregation paths of incoming data envelopes go here
 * @property data A ByteArray containing the data itself
 */
data class DataEnvelope(
        // A Tag is assigned to data as soon as it is ingested
        val tag: String = UUID.randomUUID().toString().replace("-",""),
        // History should be logged by each processor in the flow
        var history: MutableList<DataEvent> = mutableListOf(),
        // After an aggregation, need to keep track of the different history "paths"
        var paths: MutableList<List<DataEvent>> = mutableListOf()) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataEnvelope

        if (tag != other.tag) return false
        if (history != other.history) return false

        return true
    }

    fun copy() : DataEnvelope {
        val tag = this.tag
        val history = this.history.map { it.copy(tag) }.toMutableList()
        val paths = this.paths.map{path -> path.map {it.copy(tag)}}.toMutableList()
        return DataEnvelope(tag = tag, history = history, paths = paths)
    }

    override fun hashCode(): Int {
        var result = tag.hashCode()
        result = 31 * result + history.hashCode()
        return result
    }
}