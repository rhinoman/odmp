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