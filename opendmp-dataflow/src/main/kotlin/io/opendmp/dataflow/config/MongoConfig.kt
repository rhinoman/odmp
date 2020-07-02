package io.opendmp.dataflow.config
import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import io.opendmp.dataflow.model.DataflowModel
import io.opendmp.dataflow.model.ProcessorModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@Configuration
@EnableReactiveMongoRepositories(
        basePackageClasses = [DataflowModel::class, ProcessorModel::class])
class MongoConfig @Autowired constructor(private val mongoProperties: MongoProperties) :
        AbstractReactiveMongoConfiguration() {

    override fun reactiveMongoClient() = mongoClient()

    @Bean
    fun mongoClient() : MongoClient {
        val credentials: String = mongoProperties.username + ":" + mongoProperties.password.joinToString("")
        val connectionString = "mongodb://$credentials" +
                "@${mongoProperties.host}:${mongoProperties.port}/?authSource=${mongoProperties.authenticationDatabase}"
        return MongoClients.create(connectionString)
    }

    override fun getDatabaseName(): String {
            return mongoProperties.database
    }

}