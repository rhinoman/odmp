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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@ExtendWith(SpringExtension::class)
@WebFluxTest(LookupController::class)
@ComponentScan(basePackages = [
    "import com.c4_soft.springaddons.security.oauth2.test.webflux"
])

class LookupControllerTest(
        @Autowired val client: WebTestClient
) {
    private val baseUri: String = "/dataflow_api/lookup"

    @MockBean
    lateinit var reactiveJwtDecoder: ReactiveJwtDecoder

    @Test
    @WithMockAuthentication(name = "odmp-user", authorities = ["user"])
    fun `should get a list of processor types`() {
        val response = client
                .get().uri("$baseUri/processor_types")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<List<String>>()
                .returnResult()
        val list = response.responseBody
        assertNotNull(list)
        assertTrue(list!!.contains("INGEST"))
        assertTrue(list.contains("EXPORT"))
    }

    @Test
    @WithMockAuthentication(name = "odmp-user", authorities = ["user"])
    fun `should get a list of Trigger types`() {
        val response = client
                .get().uri("$baseUri/trigger_types")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<List<String>>()
                .returnResult()
        val list = response.responseBody
        assertNotNull(list)
        assertTrue(list!!.contains("AUTOMATIC"))
        assertTrue(list.contains("MANUAL"))
    }

}