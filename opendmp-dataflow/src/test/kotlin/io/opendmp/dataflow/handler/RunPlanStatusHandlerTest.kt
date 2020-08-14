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

package io.opendmp.dataflow.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.opendmp.common.message.CollectionCompleteMessage
import io.opendmp.common.model.Result
import io.opendmp.common.model.properties.DestinationType
import io.opendmp.dataflow.TestUtils
import io.opendmp.dataflow.model.CollectionModel
import io.opendmp.dataflow.model.DataflowModel
import io.opendmp.dataflow.model.DatasetModel
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findAllAndRemove
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.Duration
import java.time.Instant
import java.util.*

@SpringBootTest
@ExtendWith(SpringExtension::class)
class RunPlanStatusHandlerTest @Autowired constructor(
        private val runPlanStatusHandler: RunPlanStatusHandler,
        private val mongoTemplate: ReactiveMongoTemplate
) {

    @MockBean
    lateinit var reactiveJwtDecoder: ReactiveJwtDecoder

    @AfterEach
    fun cleanUp() {
        mongoTemplate.findAllAndRemove<DataflowModel>(Query())
        mongoTemplate.findAllAndRemove<CollectionModel>(Query())
        mongoTemplate.findAllAndRemove<DatasetModel>(Query())
    }

    private val mapper = jacksonObjectMapper()

    init {
        mapper.findAndRegisterModules()
    }

    @Test
    fun `should generate a Dataset in response to a CollectionComplete message`() {
        val dataflow = TestUtils.createBasicDataflow("TheBigDataflow")
        mongoTemplate.save(dataflow).block()
        val collection = TestUtils.createBasicCollection("My Least Favorite Collection")
        mongoTemplate.save(collection).block()
        val ccm = CollectionCompleteMessage(
                requestId = UUID.randomUUID().toString(),
                collectionId = collection.id,
                flowId = dataflow.id,
                destinationType = DestinationType.FOLDER,
                location = "/tmp/out",
                processorId = UUID.randomUUID().toString(),
                timeStamp = Instant.now(),
                result = Result.SUCCESS,
                prefix = "The Record Prefix"
        )
        val jsonString = mapper.writeValueAsString(ccm)
        runBlocking {
            val ds = runPlanStatusHandler.receiveCollectStatus(jsonString)
            while(!ds!!.isDisposed) { Thread.sleep(100) }
        }

        val dataset = mongoTemplate.find<DatasetModel>(
                Query(Criteria.where("collectionId").isEqualTo(collection.id))
        ).blockLast()
        assertNotNull(dataset)
        assertEquals("/tmp/out", dataset!!.location)
        println(dataset.name)
    }
}