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

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate

@Configuration
@Profile("!test")
class ElasticConfig : AbstractElasticsearchConfiguration() {

    @Value("\${odmp.elastic.host}")
    lateinit var host: String

    @Value("\${odmp.elastic.port}")
    lateinit var port: String

    @Value("\${odmp.elastic.protocol}")
    lateinit var protocol: String

    @Bean
    override fun elasticsearchClient(): RestHighLevelClient {

        val elasticHost = HttpHost(host, port.toInt(), protocol)
        val builder = RestClient.builder(elasticHost)
        return RestHighLevelClient(builder)
    }

    @Bean
    fun elasticsearchTemplate() : ElasticsearchRestTemplate {
        return ElasticsearchRestTemplate(elasticsearchClient())
    }

}