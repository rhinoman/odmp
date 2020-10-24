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

import io.opendmp.processor.exception.ServiceCallException
import org.apache.camel.CamelException
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ServiceCallResponseProcessor : Processor {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun process(exchange: Exchange?) {
        val body = exchange?.getIn()?.body as ByteArray
        val headers = exchange.getIn().headers
        val responseCode = headers["CamelHttpResponseCode"].toString().toInt() ?: 200
        if(responseCode >= 400) {
            val message = String(body, Charsets.UTF_8)
            throw ServiceCallException(message)
        }
    }
}