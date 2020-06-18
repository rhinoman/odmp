package io.opendmp.dataflow.api.controller

import io.opendmp.dataflow.TestUtils
import io.opendmp.dataflow.api.request.CreateDataflowRequest
import io.opendmp.dataflow.model.DataflowModel
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataflowControllerTest(@Autowired val client: WebTestClient) {
    private val testUtils = TestUtils()

    private val baseUri : String = "/dataflow_api/dataflow"

    @Test
    fun `should create a new basic dataflow`() {
        val request = testUtils.dataEntity(
                testUtils.genUserHeaders(""),
                CreateDataflowRequest(name = "FOOBAR")
        )
        val response = client.post().uri(baseUri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CreateDataflowRequest(name = "FOOBAR"))
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<DataflowModel>()
                .returnResult()
        val model = response.responseBody
        assertEquals("FOOBAR", model?.name)
    }

}