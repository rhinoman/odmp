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

package io.opendmp.dataflow

import com.amazonaws.services.s3.AmazonS3
import io.opendmp.dataflow.api.controller.PluginController
import io.opendmp.dataflow.config.S3Config
import io.opendmp.dataflow.messaging.ProcessRequester
import io.opendmp.dataflow.messaging.RunPlanDispatcher
import org.apache.camel.ProducerTemplate
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder

@Configuration
@Profile("test")
class TestConfig {
    @MockBean
    lateinit var reactiveJwtDecoder: ReactiveJwtDecoder

    @MockBean
    lateinit var runPlanDispatcher: RunPlanDispatcher

    @MockBean
    lateinit var processRequester: ProcessRequester

    @MockBean
    lateinit var s3Client: AmazonS3

    @MockBean
    lateinit var s3Config: S3Config

    @MockBean
    lateinit var producerTemplate: ProducerTemplate

    @MockBean
    lateinit var pluginController: PluginController
}