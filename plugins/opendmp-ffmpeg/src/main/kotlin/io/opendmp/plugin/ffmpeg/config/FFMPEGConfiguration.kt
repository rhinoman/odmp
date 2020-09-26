package io.opendmp.plugin.ffmpeg.config

import io.opendmp.common.model.ProcessorType
import io.opendmp.common.model.plugin.FieldType
import io.opendmp.common.model.plugin.PluginConfiguration
import io.opendmp.sdk.plugin.config.PluginConfigurationProvider
import io.opendmp.sdk.plugin.config.RegistryConfig
import org.apache.camel.CamelContext
import org.apache.camel.cloud.ServiceRegistry
import org.apache.camel.component.consul.cloud.ConsulServiceRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FFMPEGConfiguration(@Autowired private val camelContext: CamelContext) : PluginConfigurationProvider {

    private val fields = mapOf(
            "command" to FieldType.STRING,
            "timeout" to FieldType.NUMBER)

    @Value("\${odmp.plugin.name}")
    lateinit var pluginName: String

    override fun pluginConfiguration() = PluginConfiguration(
            serviceName = pluginName,
            displayName = "FFMPEG Processor",
            type = ProcessorType.EXTERNAL,
            fields = fields)
}