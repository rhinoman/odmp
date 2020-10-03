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

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
class S3Config {

    @Value("\${odmp.s3.endpoint}")
    lateinit var endpoint: String

    @Value("\${odmp.s3.region}")
    lateinit var region: String

    @Value("\${odmp.s3.access-key}")
    lateinit var accessKey: String

    @Value("\${odmp.s3.secret-key}")
    lateinit var secretKey: String

    private fun getBasicS3ClientBuilder(): AmazonS3ClientBuilder {
        val builder: AmazonS3ClientBuilder = AmazonS3Client.builder()
        if(endpoint.isNotBlank()) {
            builder.setEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(endpoint, region))
        } else {
            builder.region = region
        }
        builder.credentials = AWSStaticCredentialsProvider(BasicAWSCredentials(accessKey, secretKey))
        return builder
    }

    @Bean
    fun s3Client() : AmazonS3 {
        val builder = getBasicS3ClientBuilder()
        builder.clientConfiguration = ClientConfiguration().withSignerOverride("S3SignerType")
        builder.enablePathStyleAccess()
        return builder.build()
    }

}