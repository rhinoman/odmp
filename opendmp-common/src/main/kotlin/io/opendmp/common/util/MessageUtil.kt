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

package io.opendmp.common.util

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object MessageUtil {

    val mapper = jacksonObjectMapper()

    val log: Logger = LoggerFactory.getLogger(javaClass)

    inline fun <reified T : Any> extractMessageFromString(data: String) : T? {
        return try {
            mapper.readValue<T>(data)
        } catch (jpe: JsonProcessingException) {
            log.error("Error extracting message", jpe)
            null
        }
    }

}