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

import com.amazonaws.services.s3.AmazonS3
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.opendmp.common.message.CollectionCompleteMessage
import io.opendmp.common.message.RunPlanFailureMessage
import io.opendmp.common.message.RunPlanStartFailureMessage
import io.opendmp.common.model.DataEvent
import io.opendmp.common.model.DataEventType
import io.opendmp.common.model.ProcessorType
import io.opendmp.common.model.Result
import io.opendmp.common.model.properties.DestinationType
import io.opendmp.dataflow.TestUtils
import io.opendmp.dataflow.api.controller.PluginController
import io.opendmp.dataflow.messaging.RunPlanDispatcher
import io.opendmp.dataflow.model.CollectionModel
import io.opendmp.dataflow.model.DataflowModel
import io.opendmp.dataflow.model.DatasetModel
import io.opendmp.dataflow.model.runplan.RunPlanModel
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findAllAndRemove
import org.springframework.data.mongodb.core.findById
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

    @AfterEach
    fun cleanUp() {
        mongoTemplate.findAllAndRemove<DataflowModel>(Query())
        mongoTemplate.findAllAndRemove<CollectionModel>(Query())
        mongoTemplate.findAllAndRemove<DatasetModel>(Query())
        mongoTemplate.findAllAndRemove<RunPlanModel>(Query())
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

        val tag = UUID.randomUUID().toString()
        val history: List<List<DataEvent>> = listOf(listOf(DataEvent(
                dataTag = tag,
                eventType = DataEventType.INGESTED,
                processorId = UUID.randomUUID().toString(),
                processorName = "THE INGESTINATOR")))

        val ccm = CollectionCompleteMessage(
                requestId = UUID.randomUUID().toString(),
                collectionId = collection.id,
                flowId = dataflow.id,
                destinationType = DestinationType.FOLDER,
                location = "/tmp/out",
                processorId = UUID.randomUUID().toString(),
                timeStamp = Instant.now(),
                result = Result.SUCCESS,
                prefix = "The Record Prefix",
                dataTag = tag,
                history = history)
        val jsonString = mapper.writeValueAsString(ccm)
        runBlocking {
            val ds = runPlanStatusHandler.receiveCollectStatus(jsonString)
            while (!ds!!.isDisposed) {
                Thread.sleep(300)
            }
            Thread.sleep(300)
            val dataset = mongoTemplate.find<DatasetModel>(
                    Query(Criteria.where("collectionId").isEqualTo(collection.id))
            ).blockLast()
            assertNotNull(dataset)
            assertEquals("/tmp/out", dataset!!.location)
            println(dataset.name)
        }
    }

    @Test
    fun `should update RunPlan in response to a failure`() {
        val dataflow = TestUtils.createBasicDataflow("TheBigDataflow")
        mongoTemplate.save(dataflow).block()
        val collection = TestUtils.createBasicCollection("My Least Favorite Collection")
        mongoTemplate.save(collection).block()
        val processor = TestUtils.createBasicProcessor("Foobar", dataflow.id,1,1,ProcessorType.SCRIPT)

        val runPlan = RunPlanModel.createRunPlan(dataflow, listOf(processor))
        mongoTemplate.save(runPlan).block()

        val fm = RunPlanFailureMessage(
                requestId = UUID.randomUUID().toString(),
                runPlanId = runPlan.id,
                errorMessage = "It done broke",
                time = Instant.now(),
                processorId = processor.id
        )
        val jsonString = mapper.writeValueAsString(fm)
        runBlocking {
            val ds = runPlanStatusHandler.receiveFailureStatus(jsonString)
            while (!ds!!.isDisposed) {
                Thread.sleep(300)
            }
            Thread.sleep(300)
            val updatedRunPlan = mongoTemplate.findById<RunPlanModel>(runPlan.id).block()
            assertNotNull(updatedRunPlan!!.errors)
            assertEquals(1, updatedRunPlan.errors.size)
            val error = updatedRunPlan.errors[fm.requestId]
            assertEquals("It done broke", error!!.errorMessage)
        }
    }

    @Test
    fun `should update Dataflow in response to a start failure`() {
        val dataflow = TestUtils.createBasicDataflow("TheBigDataflow")
        mongoTemplate.save(dataflow).block()
        val processor = TestUtils.createBasicProcessor("Foobar", dataflow.id,1,1,ProcessorType.SCRIPT)

        val runPlan = RunPlanModel.createRunPlan(dataflow, listOf(processor))
        mongoTemplate.save(runPlan).block()

        val fm = RunPlanStartFailureMessage(
                requestId = UUID.randomUUID().toString(),
                runPlanId = runPlan.id,
                time = Instant.now(),
                errorMessage = "It done broke."
        )
        val jsonString = mapper.writeValueAsString(fm)
        runBlocking {
            val ds = runPlanStatusHandler.receiveStartFailure(jsonString)
            while (!ds!!.isDisposed) {
                Thread.sleep(300)
            }
            Thread.sleep(300)
            val rp = mongoTemplate.findById<RunPlanModel>(runPlan.id).block()
            assertNull(rp)
            val flow = mongoTemplate.findById<DataflowModel>(runPlan.flowId).block()
            assertEquals(false, flow!!.enabled)
        }
    }

}