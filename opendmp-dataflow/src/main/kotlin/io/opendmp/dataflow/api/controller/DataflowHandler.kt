package io.opendmp.dataflow.api.controller


import io.opendmp.dataflow.model.DataflowModel
import io.opendmp.dataflow.repository.DataflowRepository
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/dataflow_api/dataflow")
class DataflowHandler(private val dataflowRepository: DataflowRepository) {

    @GetMapping()
    fun findAll() : Flux<DataflowModel> {
        return dataflowRepository.findAll()
    }

    @GetMapping("/{id}")
    fun findOne(@PathVariable("id") id: String) : Mono<DataflowModel> {
        return dataflowRepository.findById(id)
    }

}