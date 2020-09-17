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

package io.opendmp.processor.run.processors

import io.opendmp.common.model.DataEvent
import io.opendmp.common.model.DataEventType
import io.opendmp.common.model.ProcessorRunModel
import io.opendmp.processor.domain.DataEnvelope
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.slf4j.LoggerFactory

/**
 * Takes in raw data that has just been ingested,
 * wraps it, and tags it.
 */
class DataWrapper(val sp: ProcessorRunModel) : Processor {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun process(exchange: Exchange?) {
        val dataEnvelope = DataEnvelope()
        log.info("Received new data from ingestor ${sp.name} with id ${sp.id}, tagging with id ${dataEnvelope.tag}")
        dataEnvelope.history.add(
                DataEvent(dataTag = dataEnvelope.tag,
                          eventType = DataEventType.INGESTED,
                          processorId = sp.id,
                          processorName = sp.name))
        exchange?.setProperty("dataEnvelope", dataEnvelope)
    }
}