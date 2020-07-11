package io.opendmp.dataflow.api.controller

import io.opendmp.dataflow.api.request.CreateProcessorRequest
import io.opendmp.dataflow.model.ProcessorModel
import io.opendmp.dataflow.service.ProcessorService
import org.springframework.security.core.Authentication

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

import javax.validation.Valid

@RestController
@RequestMapping("/dataflow_api/processor")
class ProcessorController(private val processorService: ProcessorService) {

    @PostMapping
    fun insertOne(@Valid @RequestBody data : CreateProcessorRequest,
                  authentication: Authentication) : Mono<ProcessorModel> {
        return processorService.createProcessor(data, authentication)
    }


}