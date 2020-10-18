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

import io.opendmp.dataflow.model.event.EventModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.mongodb.core.ChangeStreamOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Service

@Service
class EventService(private val mongoTemplate: ReactiveMongoTemplate) {

    // TODO: Additional filtering (security, events we don't care about, etc.)
    suspend fun eventStream() : Flow<EventModel> {
        return mongoTemplate.changeStream(ChangeStreamOptions.empty(), Any::class.java).map {
            EventModel(
                    eventType = it.operationType,
                    data = it.body,
                    dataType = it.collectionName,
                    timestamp = it.timestamp)
        }.asFlow()
    }

}