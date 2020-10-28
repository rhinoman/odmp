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

package io.opendmp.processor

import io.opendmp.processor.config.RedisConfig
import io.opendmp.processor.handler.RunPlanRequestHandler
import io.opendmp.processor.messaging.RunPlanRequestRouter
import io.opendmp.processor.messaging.RunPlanStatusDispatcher
import org.apache.camel.ProducerTemplate
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.redis.core.RedisTemplate

@Configuration
@Profile("test")
class TestConfig {
    @MockBean
    lateinit var  redisConfig: RedisConfig

    @MockBean
    lateinit var runPlanRequestHandler: RunPlanRequestHandler

    @MockBean
    lateinit var runPlanRequestRouter: RunPlanRequestRouter

    @MockBean
    lateinit var runPlanStatusDispatcher: RunPlanStatusDispatcher

    @MockBean
    lateinit var producerTemplate: ProducerTemplate

    @MockBean
    lateinit var consulAutoServiceRegistration: ConsulAutoServiceRegistration

    @MockBean
    lateinit var redisTemplate: RedisTemplate<String, String>

    @MockBean
    lateinit var esClient: RestHighLevelClient

}