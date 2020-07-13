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

package io.opendmp.dataflow.config
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration

@Configuration
class MongoConfig @Autowired constructor(private val mongoProperties: MongoProperties) :
        AbstractReactiveMongoConfiguration() {

    override fun reactiveMongoClient() = mongoClient()

    @Bean
    fun mongoClient() : MongoClient {
        val connectionString = ConnectionString(
                "mongodb://${mongoProperties.host}:${mongoProperties.port}"
        )
        val setBuilder = MongoClientSettings.builder()
        setBuilder.applyConnectionString(connectionString)
        if(mongoProperties.username.isNotBlank()){
            val credential = MongoCredential.createCredential(
                    mongoProperties.username,
                    mongoProperties.authenticationDatabase,
                    mongoProperties.password
            )
            setBuilder.credential(credential)
        }
        return MongoClients.create(setBuilder.build())
    }

    override fun getDatabaseName(): String {
            return mongoProperties.database
    }

}