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

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockAuthentication
import io.opendmp.dataflow.TestUtils
import io.opendmp.dataflow.api.request.CreateProcessorRequest
import io.opendmp.dataflow.config.MongoConfig
import io.opendmp.dataflow.model.ProcessorModel
import io.opendmp.dataflow.model.ProcessorType
import io.opendmp.dataflow.service.ProcessorService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@ExtendWith(SpringExtension::class)
@WebFluxTest(ProcessorController::class)
@ComponentScan(basePackages = [
    "io.opendmp.dataflow.service",
    "import com.c4_soft.springaddons.security.oauth2.test.webflux"
])
@ContextConfiguration(classes = [MongoConfig::class, ProcessorController::class])
@EnableConfigurationProperties(MongoProperties::class)
class ProcessorControllerTest(
        @Autowired val processorService: ProcessorService,
        @Autowired val client: WebTestClient,
        @Autowired val mongoTemplate: ReactiveMongoTemplate
) {

    private val baseUri : String = "/dataflow_api/processor"

    @MockBean
    lateinit var reactiveJwtDecoder: ReactiveJwtDecoder

    @Test
    @WithMockAuthentication(name = "odmp-user", authorities = ["user"])
    fun `should create a new processor`(){
        val flow = TestUtils.createBasicDataflow("FLOW1", mongoTemplate)
        val req = CreateProcessorRequest(
                name = "FOOBAR",
                flowId = flow.id,
                phase = 1,
                order = 1,
                type = ProcessorType.INGEST)

        val response = client.mutateWith(csrf())
                .post().uri(baseUri)
                .bodyValue(req)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<ProcessorModel>()
                .returnResult()
        val model = response.responseBody
        assertNotNull(model)
        assertEquals("FOOBAR", model?.name)
    }

    @Test
    @WithMockAuthentication(name = "odmp-user", authorities = ["user"])
    fun `should be able to get a processor`() {
        val flow = TestUtils.createBasicDataflow("FLOW1", mongoTemplate)
        val proc = TestUtils.createBasicProcessor("proc1", flow.id, 1,1,ProcessorType.TRANSFORM, mongoTemplate)

        val response = client.get()
                .uri(baseUri + "/" + proc.id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<ProcessorModel>()
                .returnResult()
        assertNotNull(response.responseBody)
    }

    @Test
    @WithMockAuthentication(name = "odmp-user", authorities = ["user"])
    fun `should be able to delete a processor`() {
        val flow = TestUtils.createBasicDataflow("FLOW1", mongoTemplate)
        val proc = TestUtils.createBasicProcessor("proc1", flow.id, 1,1,ProcessorType.TRANSFORM, mongoTemplate)

        val response = client.mutateWith(csrf())
                .delete()
                .uri(baseUri + "/" + proc.id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<Any>()
                .returnResult()
        assertNotNull(response.responseBody)
    }

}