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

package io.opendmp.dataflow.service

import com.amazonaws.services.s3.AmazonS3
import io.opendmp.common.model.ProcessorType
import io.opendmp.common.model.SourceModel
import io.opendmp.common.model.SourceType
import io.opendmp.dataflow.TestUtils
import io.opendmp.dataflow.model.DataflowModel
import io.opendmp.dataflow.model.ProcessorModel
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findAllAndRemove
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.remove
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class RunPlanServiceTest @Autowired constructor(
        private val runPlanService: RunPlanService,
        private val mongoTemplate: ReactiveMongoTemplate
) {

    @MockBean
    lateinit var reactiveJwtDecoder: ReactiveJwtDecoder

    @MockBean
    lateinit var s3Client: AmazonS3

    @AfterEach
    fun cleanUp() {
        mongoTemplate.findAllAndRemove<DataflowModel>(Query())
        mongoTemplate.findAllAndRemove<ProcessorModel>(Query())
    }

    fun createDataflow() : DataflowModel {
        val dataflow = TestUtils.createBasicDataflow("Foobar")
        mongoTemplate.insert(dataflow).block()

        dataflow.enabled = true
        mongoTemplate.save(dataflow).block()

        return dataflow
    }

    @Test
    fun `should generate a runplan`() = runBlocking<Unit> {
        val dataflow = createDataflow()
        val proc1 = TestUtils.createBasicProcessor("Foo1", dataflow.id, 1, 1, ProcessorType.INGEST)
        val proc2 = TestUtils.createBasicProcessor("Foo2", dataflow.id, 2, 1, ProcessorType.COLLECT)
        mongoTemplate.insert(proc1).block()
        mongoTemplate.insert(proc2).block()
        proc1.inputs = listOf(SourceModel(SourceType.INGEST_FILE_DROP, "/tmp"))
        proc2.inputs = listOf(SourceModel(SourceType.PROCESSOR, proc1.id))
        mongoTemplate.save(proc1).block()
        mongoTemplate.save(proc2).block()

        val runPlan = runPlanService.generateRunPlan(dataflow)
        assertEquals(dataflow.id, runPlan.flowId)
        val depMap = runPlan.processorDependencyMap
        assertEquals(2, runPlan.processors.size, "Wrong number processors")
        assertEquals(1, depMap.size, "depMap wrong size")
        assertEquals(1, depMap[proc1.id]?.size, "proc1 deps wrong size")
        assertEquals(1, runPlan.startingProcessors.size, "wrong number startingProcessors")
    }

}