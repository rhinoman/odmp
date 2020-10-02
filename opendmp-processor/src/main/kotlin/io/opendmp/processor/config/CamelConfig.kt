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

package io.opendmp.processor.config

import org.apache.camel.component.redis.processor.idempotent.RedisIdempotentRepository
import org.apache.camel.model.cloud.ServiceCallConfigurationDefinition
import org.apache.camel.spi.IdempotentRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class CamelConfig(private val camelConsumerRedisTemplate: RedisTemplate<String, String>) {

    @Bean
    fun basicServiceCall() : ServiceCallConfigurationDefinition {
        val conf = ServiceCallConfigurationDefinition()
        conf.component = "undertow"

        return conf
    }

    @Bean
    fun idempotentRepo(): IdempotentRepository {
        return RedisIdempotentRepository.redisIdempotentRepository(camelConsumerRedisTemplate,"camel-repo")
    }

}