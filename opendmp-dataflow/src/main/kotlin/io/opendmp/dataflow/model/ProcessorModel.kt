package io.opendmp.dataflow.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "processors")
data class ProcessorModel(@Id val id : String = ObjectId.get().toHexString(),
                          val flowId : String,
                          val name: String,
                          val description: String? = null,
                          var status : DataFlowStatus = DataFlowStatus.IDLE,
                          var health : HealthModel = HealthModel(),
                          var phase: Int,
                          var order: Int = 1,
                          val type: ProcessorType,
                          @CreatedDate
                          val createdOn: Instant = Instant.now(),
                          @LastModifiedDate
                          var updatedOn: Instant = Instant.now()) {
}