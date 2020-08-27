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

package io.opendmp.processor.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.opendmp.processor.domain.RunPlanRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer

@Configuration
class RedisConfig {

    @Value("\${odmp.redis.host}")
    lateinit var redisHost: String

    @Value("\${odmp.redis.port}")
    lateinit var redisPort: Number

    @Bean
    fun redisConnectionFactory() : RedisConnectionFactory {
        return LettuceConnectionFactory(redisHost, redisPort.toInt())
    }

    @Bean
    fun runPlanRedisTemplate() : RedisTemplate<String, RunPlanRecord> {
        val valueSerializer = Jackson2JsonRedisSerializer(RunPlanRecord::class.java)
        val mapper = jacksonObjectMapper()
        mapper.findAndRegisterModules()
        valueSerializer.setObjectMapper(mapper)
        val template = RedisTemplate<String, RunPlanRecord>()
        template.setConnectionFactory(redisConnectionFactory())
        template.setDefaultSerializer(valueSerializer)
        return template
    }

}