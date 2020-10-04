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
import com.mongodb.client.result.DeleteResult
import io.opendmp.dataflow.TestConfig
import io.opendmp.dataflow.TestUtils
import io.opendmp.dataflow.api.request.CreateCollectionRequest
import io.opendmp.dataflow.config.MongoConfig
import io.opendmp.dataflow.messaging.ProcessRequester
import io.opendmp.dataflow.messaging.RunPlanDispatcher
import io.opendmp.dataflow.model.CollectionModel
import io.opendmp.dataflow.model.DatasetModel
import io.opendmp.dataflow.service.CollectionService
import io.opendmp.dataflow.service.RunPlanService
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
import org.springframework.data.mongodb.core.findAllAndRemove
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@ExtendWith(SpringExtension::class)
@WebFluxTest(CollectionController::class)
@ComponentScan(basePackages = [
    "io.opendmp.dataflow.service",
    "io.opendmp.dataflow.messaging",
    "import com.c4_soft.springaddons.security.oauth2.test.webflux"
])
@ContextConfiguration(classes = [MongoConfig::class, CollectionController::class, TestConfig::class])
@EnableConfigurationProperties(MongoProperties::class)
class CollectionControllerTest @Autowired constructor(
        val collectionService: CollectionService,
        val client: WebTestClient,
        val mongoTemplate: ReactiveMongoTemplate
) {

    private val baseUri: String = "/dataflow_api/collection"

    @AfterEach
    fun cleanUp() {
        mongoTemplate.findAllAndRemove<CollectionModel>(Query()).blockLast()
    }

    @Test
    @WithMockAuthentication(name = "odmp-user", authorities = ["user"])
    fun `should create a new basic collection`() {

        val response = client.mutateWith(csrf())
                .post().uri(baseUri)
                .bodyValue(CreateCollectionRequest(name = "FOOBAR"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<CollectionModel>()
                .returnResult()

        val model = response.responseBody
        assertEquals("FOOBAR", model?.name)
    }

    @Test
    @WithMockAuthentication(name = "odmp-user", authorities = ["user"])
    fun `should return a list of collections`(){
        val collections = listOf(
                CollectionModel(name = "FOOBAR", creator = "odmp-user", group = "NONSUCH"),
                CollectionModel(name = "FOOBAR2", creator = "odmp-user", group= "NONSUCH2")
        )
        mongoTemplate.insertAll<CollectionModel>(collections).blockLast()
        val response = client.get().uri(baseUri)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<List<CollectionModel>>()
                .returnResult()
        val list = response.responseBody
        assertEquals(2, list?.size)
        assertEquals("FOOBAR", list!![0].name)
    }

    @Test
    @WithMockAuthentication(name = "odmp-user", authorities = ["user"])
    fun `should return a single collection`(){
        val collection = CollectionModel(name = "FOOBAR", creator = "odmp-user", group = "NONSUCH")
        mongoTemplate.insert(collection).block()
        val response = client.get().uri(baseUri + "/" + collection.id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<CollectionModel>()
                .returnResult()

        val coll = response.responseBody
        assertEquals("FOOBAR", coll!!.name)
    }

    @Test
    @WithMockAuthentication(name = "odmp-user", authorities = ["user"])
    fun `should delete a single collection`(){
        val collection = CollectionModel(name = "FOOBAR", creator = "odmp-user", group = "NONSUCH")
        mongoTemplate.insert(collection).block()
        val response = client.mutateWith(csrf())
                .delete().uri(baseUri + "/" + collection.id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<Any>()
                .returnResult()

        val res = response.responseBody
        assertNotNull(res)
    }

    @Test
    @WithMockAuthentication(name = "odmp-user", authorities = ["user"])
    fun `should return a list of datasets`(){
        val collection = CollectionModel(name = "FOOBAR", creator = "odmp-user", group = "NONSUCH")
        mongoTemplate.insert(collection).block()
        val datasets = TestUtils.createBasicDatasets(collection.id, 5)
        mongoTemplate.insertAll<DatasetModel>(datasets).blockLast()
        val response = client.get().uri(baseUri + "/${collection.id}/datasets")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<List<DatasetModel>>()
                .returnResult()
        assertNotNull(response.responseBody)

        assertEquals(5, response.responseBody?.size)
    }

}