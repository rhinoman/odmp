package io.opendmp.dataflow.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "dataflows")
class DataflowModel(@Id val id : String = ObjectId.get().toHexString(),
                    val name : String,
                    val description : String?,
                    val group : String?,
                    val creator : String?,
                    val status : DataFlowStatus = DataFlowStatus.IDLE,
                    var health : HealthModel = HealthModel(),
                    val createdOn: LocalDateTime = LocalDateTime.now(),
                    var updatedOn: LocalDateTime = LocalDateTime.now()) {
}