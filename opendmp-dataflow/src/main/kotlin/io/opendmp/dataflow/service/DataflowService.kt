package io.opendmp.dataflow.service

import io.opendmp.dataflow.api.request.CreateDataflowRequest
import io.opendmp.dataflow.model.DataflowModel
import io.opendmp.dataflow.repository.DataflowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class DataflowService (private val dataflowRepository: DataflowRepository) {

    fun createDataflow(data : CreateDataflowRequest) : Mono<DataflowModel> {

        val dataflow = DataflowModel(
                name = data.name,
                description = data.description,
                group = data.group,
                creator = "")
        return dataflowRepository.save(dataflow)
    }

    fun get(id: String) : Mono<DataflowModel> {
        return dataflowRepository.findById(id)
    }

    suspend fun getList() : Flow<DataflowModel> {
        return dataflowRepository.findAll().asFlow()
    }

    fun delete(id: String) : Mono<Void> {
        return dataflowRepository.deleteById(id)
    }

}