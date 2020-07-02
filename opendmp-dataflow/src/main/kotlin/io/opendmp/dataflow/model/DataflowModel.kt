package io.opendmp.dataflow.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "dataflows")
class DataflowModel(@Id val id : String = ObjectId.get().toHexString(),
                    var name : String,
                    var description : String?,
                    var group : String?,
                    val creator : String?,
                    var status : DataFlowStatus = DataFlowStatus.IDLE,
                    var health : HealthModel = HealthModel(),
                    @CreatedDate
                    val createdOn: Instant = Instant.now(),
                    @LastModifiedDate
                    var updatedOn: Instant = Instant.now()) {
}