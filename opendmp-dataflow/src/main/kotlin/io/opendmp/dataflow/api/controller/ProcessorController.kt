/*
 * Copyright (c) 2020. The Open Data Management Platform contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opendmp.dataflow.api.controller

import com.mongodb.client.result.DeleteResult
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

    @GetMapping("/{id}")
    fun findOne(@PathVariable("id") id: String) : Mono<ProcessorModel> {
        return processorService.get(id)
    }

    @DeleteMapping("/{id}")
    fun deleteOne(@PathVariable("id") id: String) : Mono<DeleteResult> {
        return processorService.deleteProcessor(id)
    }


}