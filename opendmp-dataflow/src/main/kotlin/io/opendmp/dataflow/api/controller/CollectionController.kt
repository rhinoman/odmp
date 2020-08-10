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
import io.opendmp.dataflow.api.request.CreateCollectionRequest
import io.opendmp.dataflow.model.CollectionModel
import io.opendmp.dataflow.model.DatasetModel
import io.opendmp.dataflow.service.CollectionService
import kotlinx.coroutines.flow.Flow
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import javax.validation.Valid

@RestController
@RequestMapping("/dataflow_api/collection")
class CollectionController(private val collectionService: CollectionService) {

    @GetMapping
    suspend fun findAll() : Flow<CollectionModel> {
        return collectionService.getList()
    }

    @GetMapping("/{id}")
    fun findOne(@PathVariable("id") id: String) : Mono<CollectionModel> {
        return collectionService.get(id)
    }

    @PostMapping
    fun insertOne(@Valid @RequestBody data : CreateCollectionRequest,
                  authentication: Authentication) : Mono<CollectionModel> {
        return collectionService.createCollection(data, authentication)
    }

    @DeleteMapping("/{id}")
    fun deleteOne(@PathVariable("id") id: String) : Mono<DeleteResult> {
        return collectionService.delete(id)
    }

    @GetMapping("/{id}/datasets")
    suspend fun getDatasets(@PathVariable("id") id: String) : Flow<DatasetModel> {
        return collectionService.getDatasets(id)
    }

}