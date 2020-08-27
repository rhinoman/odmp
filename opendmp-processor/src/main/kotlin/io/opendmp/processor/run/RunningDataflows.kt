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

package io.opendmp.processor.run

/**
 * We need to keep track of the dataflows running on this
 * ProcessorService instance.
 */
object RunningDataflows {

    private val flows: MutableMap<String, String> = mutableMapOf()

    @Synchronized
    fun add(flowId: String, runPlanId: String) {
        flows[flowId] = runPlanId
    }

    fun get() : Map<String, String> {
        return flows
    }

    fun get(flowId: String) : String? {
        return flows[flowId]
    }

    @Synchronized
    fun remove(flowId: String) : String? {
        return flows.remove(flowId)
    }
}