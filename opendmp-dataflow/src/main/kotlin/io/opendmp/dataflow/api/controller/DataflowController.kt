package io.opendmp.dataflow.api.controller

import io.opendmp.dataflow.api.exception.NotFoundException
import io.opendmp.dataflow.api.request.CreateDataflowRequest
import io.opendmp.dataflow.model.DataflowModel
import io.opendmp.dataflow.repository.DataflowRepository
import io.opendmp.dataflow.service.DataflowService
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import javax.validation.Valid

@RestController
@RequestMapping("/dataflow_api/dataflow")
class DataflowController(private val dataflowService: DataflowService) {

    @GetMapping
    fun findAll() : Flux<DataflowModel> {
        return dataflowService.getList()
    }

    @GetMapping("/{id}")
    fun findOne(@PathVariable("id") id: String) : Mono<DataflowModel> {
        return dataflowService.get(id).switchIfEmpty { throw NotFoundException() }
    }

    @PostMapping
    fun insertOne(@Valid @RequestBody data : CreateDataflowRequest) : Mono<DataflowModel> {
        return dataflowService.createDataflow(data)
    }

    @DeleteMapping("/{id}")
    fun deleteOne(@PathVariable("id") id: String) : Mono<Void> {
        return dataflowService.delete(id)
    }
}