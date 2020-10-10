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

import io.opendmp.common.message.RunPlanFailureMessage
import io.opendmp.processor.config.SpringContext
import io.opendmp.processor.messaging.RunPlanStatusDispatcher
import org.apache.camel.Exchange
import org.apache.camel.Headers
import org.apache.camel.ProducerTemplate
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.Instant
import java.util.*

class FailureHandler {

    private val runPlanStatusDispatcher: RunPlanStatusDispatcher =
            SpringContext.getBean(RunPlanStatusDispatcher::class)

    private val log = LoggerFactory.getLogger(javaClass)

    fun processFailure(@Headers headers: Map<String,Any>, exchange: Exchange, cause: Exception) {
        val runPlanId: String = headers["runPlan"] as String
        val procId: String? = headers["processor"] as String?

        val msg = RunPlanFailureMessage(
                requestId = UUID.randomUUID().toString(),
                runPlanId = runPlanId,
                processorId = procId,
                errorMessage = cause.localizedMessage,
                time = Instant.now())

        log.info("Sending Run plan failure notification for run plan: $runPlanId," +
                " with processor Id: $procId")
        runPlanStatusDispatcher.sendFailureMessage(msg)
        exchange.getIn().body = cause
    }

}