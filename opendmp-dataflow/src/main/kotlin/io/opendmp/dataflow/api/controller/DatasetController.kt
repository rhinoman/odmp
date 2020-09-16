/*
 * Copyright (c) 2020. James Adam and the Open Data Management Platform contributors.
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
import io.opendmp.dataflow.api.response.DatasetDetail
import io.opendmp.dataflow.api.response.DownloadRequestResponse
import io.opendmp.dataflow.service.DatasetService
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/dataflow_api/dataset")
class DatasetController(private val datasetService: DatasetService) {

    @GetMapping("/{id}")
    fun getDetail(@PathVariable("id") id: String) : Mono<DatasetDetail> {
        return datasetService.getDetail(id)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") id: String) : Mono<DeleteResult> {
        return datasetService.delete(id)
    }

    /**
     * Return a one time use link for downloading a file
     */
    @GetMapping("/{id}/request_download")
    fun requestDownload(@PathVariable("id") id: String) : Mono<DownloadRequestResponse> {
        return datasetService.requestDownload(id)
    }

    /**
     * Download a file
     */
    @GetMapping("/download")
    fun downloadData(@RequestParam("token") token: String) :
            Mono<ResponseEntity<InputStreamResource>> {
        return datasetService.download(token)
    }

}