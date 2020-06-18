package io.opendmp.dataflow.repository

import io.opendmp.dataflow.model.DataflowModel
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface DataflowRepository : ReactiveMongoRepository<DataflowModel, String> {

}