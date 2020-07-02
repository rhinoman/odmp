package io.opendmp.dataflow.config
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
        val connectionString = if(mongoProperties.username.isNotBlank() && mongoProperties.password.isNotEmpty()) {
            val credentials: String = mongoProperties.username + ":" + mongoProperties.password.joinToString("")
            "mongodb://$credentials" +
                    "@${mongoProperties.host}:${mongoProperties.port}/" +
                    "?authSource=${mongoProperties.authenticationDatabase}"
        } else {
            "mongodb://${mongoProperties.host}:${mongoProperties.port}"
        }
        return MongoClients.create(connectionString)
    }

    override fun getDatabaseName(): String {
            return mongoProperties.database
    }

}