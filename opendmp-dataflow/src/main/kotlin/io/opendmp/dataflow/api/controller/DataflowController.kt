package io.opendmp.dataflow.api.controller

import com.mongodb.client.result.DeleteResult
import io.opendmp.dataflow.api.request.CreateDataflowRequest
import io.opendmp.dataflow.model.DataflowModel
import io.opendmp.dataflow.service.DataflowService
import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import javax.validation.Valid

@RestController
@RequestMapping("/dataflow_api/dataflow")
class DataflowController(private val dataflowService: DataflowService) {

    @GetMapping
    suspend fun findAll() : Flow<DataflowModel> {
        return dataflowService.getList()
    }

    @GetMapping("/{id}")
    fun findOne(@PathVariable("id") id: String) : Mono<DataflowModel> {
        return dataflowService.get(id)
    }

    @PostMapping
    fun insertOne(@Valid @RequestBody data : CreateDataflowRequest) : Mono<DataflowModel> {
        return dataflowService.createDataflow(data)
    }

    @DeleteMapping("/{id}")
    fun deleteOne(@PathVariable("id") id: String) : Mono<DeleteResult> {
        return dataflowService.delete(id)
    }
}