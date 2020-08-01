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

package io.opendmp.processor.executors

import io.opendmp.processor.config.PulsarConfig
import io.opendmp.processor.config.RedisConfig
import io.opendmp.processor.handler.RunPlanRequestHandler
import io.opendmp.processor.messaging.RunPlanRequestRouter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class TestClojureExecutor {

    @MockBean
    lateinit var pulsarConfig: PulsarConfig

    @MockBean
    lateinit var  redisConfig: RedisConfig

    @MockBean
    lateinit var runPlanRequestHandler: RunPlanRequestHandler

    @MockBean
    lateinit var runPlanRequestRouter: RunPlanRequestRouter

    @Test
    fun `Clojure executor should return result as byte array`(){
        val cljEx = ClojureExecutor()
        val script = """
            (map (fn [x] (* x 2)) [1 2 3 4 5])
        """.trimIndent()
        val result = cljEx.executeScript(script)
        assertNotNull(result)
        val resultInts = result.toList().map { it.toInt() }
        assertEquals(listOf(2, 4, 6, 8, 10), resultInts)
    }

}