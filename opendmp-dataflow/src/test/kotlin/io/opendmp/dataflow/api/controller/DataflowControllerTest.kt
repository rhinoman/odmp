package io.opendmp.dataflow.api.controller

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockAuthentication
import io.opendmp.dataflow.TestUtils
import io.opendmp.dataflow.api.request.CreateDataflowRequest
import io.opendmp.dataflow.config.MongoConfig
import io.opendmp.dataflow.model.DataflowModel
import io.opendmp.dataflow.service.DataflowService
import kotlinx.coroutines.reactive.awaitLast
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@ExtendWith(SpringExtension::class)
@WebFluxTest(DataflowController::class)
@ComponentScan(basePackages = [
    "io.opendmp.dataflow.service",
    "import com.c4_soft.springaddons.security.oauth2.test.webflux"
])
@ContextConfiguration(classes = [MongoConfig::class, DataflowController::class])
@EnableConfigurationProperties(MongoProperties::class)
class DataflowControllerTest(
        @Autowired val dataflowService: DataflowService,
        @Autowired val client: WebTestClient,
        @Autowired val mongoTemplate: ReactiveMongoTemplate
) {

    private val testUtils = TestUtils()
    private val baseUri : String = "/dataflow_api/dataflow"

    @MockBean
    lateinit var reactiveJwtDecoder: ReactiveJwtDecoder

//    @MockBean
//    lateinit var mongoTemplate: ReactiveMongoTemplate

    @Test
    @WithMockAuthentication(name = "odmp-user", authorities = ["user"])
    fun `should create a new basic dataflow`() {
        val dataflow = DataflowModel(name = "FOOBAR", description = "THE FOOBAR", creator = "", group = "")
//        whenever(mongoTemplate.save<DataflowModel>(any()).thenReturn(Mono.just(dataflow)))

        val response = client.mutateWith(csrf())
                .post().uri(baseUri)
                .bodyValue(CreateDataflowRequest(name = "FOOBAR"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<DataflowModel>()
                .returnResult()
        val model = response.responseBody
        assertEquals("FOOBAR", model?.name)
    }

    @Test
    @WithMockAuthentication(name = "odmp-user", authorities = ["user"])
    fun `should return a list of dataflows`() {
        val dataflows = listOf(
                DataflowModel(
                        name = "FOOBAR",
                        creator = "",
                        description = "THE FOOBAR",
                        group = ""))
//        whenever(mongoTemplate.findAll<DataflowModel>()).thenReturn(Flux.fromIterable(dataflows))
        mongoTemplate.insertAll<DataflowModel>(dataflows).blockLast()
        val response = client.get().uri(baseUri)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<List<DataflowModel>>()
                .returnResult()
        val list = response.responseBody
        assertEquals("FOOBAR", list!![0].name)
    }

    @Test
    @WithMockAuthentication(name = "odmp-user", authorities = ["user"])
    fun `should return a single dataflow`() {
        val dataflow = DataflowModel(
                id = ObjectId.get().toHexString(),
                name = "FOOBAR",
                creator = "",
                description = "THE FOOBAR",
                group = "")
//        whenever(mongoTemplate.findById<DataflowModel>(any())).thenReturn(Mono.just(dataflow))
        mongoTemplate.insert<DataflowModel>(dataflow).block()
        val response = client.get().uri(baseUri + "/" + dataflow.id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<DataflowModel>()
                .returnResult()
        val df = response.responseBody
        assertEquals("FOOBAR", df!!.name)
    }

}