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

package io.opendmp.dataflow.model

import io.opendmp.common.model.ProcessorRunModel
import io.opendmp.common.model.ProcessorType
import io.opendmp.common.model.SourceModel
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "processors")
data class ProcessorModel(@Id val id : String = ObjectId.get().toHexString(),
                          @Indexed(name = "processor_flow_id_index", background = true)
                          val flowId : String,
                          var name: String,
                          var enabled: Boolean = false,
                          var description: String? = null,
                          val creator: String?,
                          var phase: Int,
                          var order: Int = 1,
                          var type: ProcessorType,
                          var triggerType: TriggerType = TriggerType.AUTOMATIC,
                          var inputs: List<SourceModel> = mutableListOf(),
                          var properties: MutableMap<String, Any>? = mutableMapOf(),
                          @CreatedDate
                          val createdOn: Instant = Instant.now(),
                          @LastModifiedDate
                          var updatedOn: Instant = Instant.now()) {

    fun toRunModel() = ProcessorRunModel(
            id = this.id,
            flowId = this.flowId,
            name = this.name,
            type = this.type,
            properties = this.properties,
            inputs = this.inputs
    )
}