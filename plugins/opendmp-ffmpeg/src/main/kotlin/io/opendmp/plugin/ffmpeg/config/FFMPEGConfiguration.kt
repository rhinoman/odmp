package io.opendmp.plugin.ffmpeg.config

import io.opendmp.common.model.ProcessorType
import io.opendmp.common.model.plugin.FieldDescription
import io.opendmp.common.model.plugin.FieldType
import io.opendmp.common.model.plugin.PluginConfiguration
import io.opendmp.sdk.plugin.config.PluginConfigurationProvider
import org.apache.camel.CamelContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class FFMPEGConfiguration(@Autowired private val camelContext: CamelContext) : PluginConfigurationProvider {

    private val fields = mapOf(
            "command" to FieldDescription(
                    type = FieldType.STRING,
                    required = true,
                    helperText = "The command line to pass to FFMPEG"),
            "timeout" to FieldDescription(
                    type = FieldType.NUMBER,
                    required = false,
                    helperText = "The amount of time (in seconds) to wait for FFMPEG to complete"))

    @Value("\${odmp.plugin.name}")
    lateinit var pluginName: String

    override fun pluginConfiguration() = PluginConfiguration(
            serviceName = pluginName,
            displayName = "FFMPEG Processor",
            type = ProcessorType.EXTERNAL,
            fields = fields)
}