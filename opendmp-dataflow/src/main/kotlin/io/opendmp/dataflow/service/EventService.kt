/*
 * Copyright (c) 2020. James Adam and the Open Data Management Platform contributors.
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

import com.mongodb.client.model.changestream.OperationType
import io.opendmp.dataflow.model.event.EventModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.mongodb.core.ChangeStreamOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.Instant
import kotlin.concurrent.fixedRateTimer

@Service
class EventService(private val mongoTemplate: ReactiveMongoTemplate) {

    // TODO: Additional filtering (security, events we don't care about, etc.)
    fun eventStream() : Flux<EventModel> {

        val cs = mongoTemplate.changeStream(ChangeStreamOptions.empty(), Any::class.java).map {
               EventModel(
                       eventType = it.operationType,
                       data = it.body,
                       dataType = it.collectionName,
                       timestamp = it.timestamp)
        }

        val heartbeat = Flux.interval(Duration.ofSeconds(7))
                .map { EventModel(
                        eventType = OperationType.OTHER,
                        data = "tick",
                        dataType = "HEARTBEAT",
                        timestamp = Instant.now())}

        return Flux.merge(cs, heartbeat)
    }
}