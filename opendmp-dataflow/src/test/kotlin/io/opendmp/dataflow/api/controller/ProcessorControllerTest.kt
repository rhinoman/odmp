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

import com.amazonaws.services.s3.AmazonS3
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockAuthentication
import io.opendmp.common.model.ProcessorType
import io.opendmp.common.model.SourceModel
import io.opendmp.common.model.SourceType
import io.opendmp.dataflow.TestConfig
import io.opendmp.dataflow.TestUtils
import io.opendmp.dataflow.api.request.CreateProcessorRequest
import io.opendmp.dataflow.api.request.UpdateProcessorRequest
import io.opendmp.dataflow.api.response.ProcessorDetail
import io.opendmp.dataflow.config.MongoConfig
import io.opendmp.dataflow.messaging.ProcessRequester
import io.opendmp.dataflow.messaging.RunPlanDispatcher
import io.opendmp.dataflow.model.DataflowModel
import io.opendmp.dataflow.model.ProcessorModel
import io.opendmp.dataflow.service.ProcessorService
import org.junit.jupiter.api.AfterEach
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
import org.springframework.data.mongodb.core.remove
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
@ContextConfiguration(classes = [MongoConfig::class, ProcessorController::class, TestConfig::class])
@EnableConfigurationProperties(MongoProperties::class)
class ProcessorControllerTest(
        @Autowired val processorService: ProcessorService,
        @Autowired val client: WebTestClient,
        @Autowired val mongoTemplate: ReactiveMongoTemplate
) {

    private val baseUri : String = "/dataflow_api/processor"

    @AfterEach
    fun cleanUp() {
        mongoTemplate.remove<ProcessorModel>()
        mongoTemplate.remove<DataflowModel>()
    }



    @Test
    @WithMockAuthentication(name = "odmp-user", authorities = ["user"])
    fun `should create a new processor`(){
        val flow = TestUtils.createBasicDataflow("FLOW1")
        mongoTemplate.save(flow).block()
        val req = CreateProcessorRequest(
                name = "FOOBAR",
                flowId = flow.id,
                phase = 1,
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
        assertEquals(1, model?.phase)
        assertEquals(1, model?.order)
    }

    @Test
    @WithMockAuthentication(name = "odmp-user", authorities = ["user"])
    fun `should be able to get a processor`() {
        val flow = TestUtils.createBasicDataflow("FLOW1")
        mongoTemplate.save(flow).block()
        val proc = TestUtils.createBasicProcessor("proc1", flow.id, 1,1,ProcessorType.SCRIPT)
        mongoTemplate.save(proc).block()

        val response = client.get()
                .uri(baseUri + "/" + proc.id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<ProcessorDetail>()
                .returnResult()
        assertNotNull(response.responseBody)
        assertEquals("FLOW1", response.responseBody?.dataflow?.name)
        assertEquals("proc1", response.responseBody?.processor?.name)
    }

    @Test
    @WithMockAuthentication(name = "odmp-user", authorities = ["user"])
    fun `should be able to update a processor`() {
        val flow = TestUtils.createBasicDataflow("FLOW1")
        mongoTemplate.save(flow).block()
        val proc = TestUtils.createBasicProcessor("proc1", flow.id, 1,1, ProcessorType.SCRIPT)
        mongoTemplate.save(proc).block()

        val updateReq = UpdateProcessorRequest(
                name = proc.name,
                description = proc.description,
                phase = proc.phase,
                order = 2,
                triggerType = proc.triggerType,
                type = proc.type,
                properties = mutableMapOf(Pair("script", "println('Hello World!');")),
                inputs = mutableListOf(SourceModel(SourceType.INGEST_FILE_DROP, "123456")),
                enabled = false
        )

        val response = client.mutateWith(csrf())
                .put().uri(baseUri + "/" + proc.id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateReq)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<ProcessorModel>()
                .returnResult()

        assertNotNull(response.responseBody)
        assertEquals(2, response.responseBody!!.order)
        assertEquals("println('Hello World!');", response.responseBody!!.properties!!["script"])
    }

    @Test
    @WithMockAuthentication(name = "odmp-user", authorities = ["user"])
    fun `should be able to delete a processor`() {
        val flow = TestUtils.createBasicDataflow("FLOW1")
        mongoTemplate.save(flow).block()
        val proc = TestUtils.createBasicProcessor("proc1", flow.id, 1,1,ProcessorType.SCRIPT)
        mongoTemplate.save(proc).block()

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