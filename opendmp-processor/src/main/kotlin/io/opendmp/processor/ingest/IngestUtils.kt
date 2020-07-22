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

package io.opendmp.processor.ingest

import io.opendmp.common.exception.NotImplementedException
import io.opendmp.common.message.ProcessRequestMessage
import io.opendmp.common.model.SourceType
import org.apache.camel.Endpoint

object IngestUtils {

    /**
     * This function sets up an ingest camel route
     */
    fun handleIngestRequest(msg: ProcessRequestMessage) {
        // An Ingest node has only one input,
        // so we just grab the first
        val input = msg.inputs.first()

        val endpoint: String = when(input.sourceType) {
            SourceType.INGEST_FILE_DROP ->
                "file://${input.sourceLocation!!}?readLock=changed"
            else -> throw NotImplementedException("SourceType ${input.sourceType} not implemented")
        }

    }

}