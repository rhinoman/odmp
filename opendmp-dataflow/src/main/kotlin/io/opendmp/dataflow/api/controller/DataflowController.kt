package io.opendmp.dataflow.api.controller

import com.mongodb.client.result.DeleteResult
import io.opendmp.dataflow.api.request.CreateDataflowRequest
import io.opendmp.dataflow.model.DataflowModel
import io.opendmp.dataflow.model.ProcessorModel
import io.opendmp.dataflow.service.DataflowService
import kotlinx.coroutines.flow.Flow
import org.springframework.security.core.Authentication
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
    fun insertOne(@Valid @RequestBody data : CreateDataflowRequest,
                  authentication: Authentication) : Mono<DataflowModel> {
        return dataflowService.createDataflow(data, authentication)
    }

    @DeleteMapping("/{id}")
    fun deleteOne(@PathVariable("id") id: String) : Mono<DeleteResult> {
        return dataflowService.delete(id)
    }

    @GetMapping("/{id}/processors")
    fun getProcessors(@PathVariable("id") id: String) : Flow<ProcessorModel> {
        return dataflowService.getProcessors(id)
    }
}