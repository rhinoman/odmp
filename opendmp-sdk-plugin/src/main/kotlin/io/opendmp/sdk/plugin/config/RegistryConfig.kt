package io.opendmp.sdk.plugin.config

import org.apache.camel.CamelContext
import org.apache.camel.cloud.ServiceRegistry
import org.apache.camel.component.consul.cloud.ConsulServiceRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RegistryConfig(@Autowired private val camelContext: CamelContext) {

    @Value("\${odmp.consul.url}")
    lateinit var consulUrl: String

    @Bean
    fun serviceRegistry() : ServiceRegistry {
        val service = ConsulServiceRegistry()
        service.url = consulUrl
        service.serviceHost = "localhost"
        camelContext.addService(service)
        return service
    }

}