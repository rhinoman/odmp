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

package io.opendmp.processor.run

import io.opendmp.processor.TestUtils
import io.opendmp.processor.config.PulsarConfig
import io.opendmp.processor.config.RedisConfig
import io.opendmp.processor.domain.RunPlan
import io.opendmp.processor.handler.RunPlanRequestHandler
import io.opendmp.processor.messaging.RunPlanRequestRouter
import org.apache.camel.CamelContext
import org.apache.camel.test.spring.junit5.CamelSpringBootTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@SpringBootTest
@ExtendWith(SpringExtension::class)
@CamelSpringBootTest
@ContextConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TestRunPlanRouteBuilder @Autowired constructor(
        private val camelContext: CamelContext) {

    @MockBean
    lateinit var pulsarConfig: PulsarConfig

    @MockBean
    lateinit var  redisConfig: RedisConfig

    @MockBean
    lateinit var runPlanRequestHandler: RunPlanRequestHandler

    @MockBean
    lateinit var runPlanRequestRouter: RunPlanRequestRouter

    fun basicRunPlan() : RunPlan {
        val iProc = TestUtils.createFileIngestProcessor("fileIn")
        val tProc = TestUtils.createScriptProcessor("script1", listOf(iProc.id))
        val procs = mapOf(iProc.id to iProc, tProc.id to tProc)
        val procDeps = mapOf(iProc.id to listOf(tProc.id))
        return RunPlan(
                id = UUID.randomUUID().toString(),
                flowId = UUID.randomUUID().toString(),
                startingProcessors = listOf(iProc.id),
                processorDependencyMap = procDeps,
                processors = procs
        )
    }

    @Test
    fun testSimpleRunPlan() {
        val runPlan = basicRunPlan()
        val routeBuilder = RunPlanRouteBuilder(runPlan)
        camelContext.addRoutes(routeBuilder)
        assertEquals(true, true)
    }

}