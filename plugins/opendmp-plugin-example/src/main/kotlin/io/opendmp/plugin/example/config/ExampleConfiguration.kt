package io.opendmp.plugin.example.config

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
class ExampleConfiguration(@Autowired private val camelContext: CamelContext) : PluginConfigurationProvider {

    private val fields = mapOf(
            "command" to FieldDescription(
                    type = FieldType.ENUM,
                    required = true,
                    helperText = "The command to execute",
                    options = listOf("cat", "ls", "grep")),
            "sudo" to FieldDescription(
                    type = FieldType.BOOLEAN,
                    required = false,
                    helperText = "Run the command as sudo?"),
            "arguments" to FieldDescription(
                    type = FieldType.STRING,
                    required = false,
                    helperText = "Additional arguments to pass to the command"),
            "code" to FieldDescription(
                    type = FieldType.CODE,
                    required = false,
                    helperText = "Some code that will not be evaluated",
                    properties = mapOf("language" to "python")
            ),
            "timeout" to FieldDescription(
                    type = FieldType.NUMBER,
                    required = false,
                    helperText = "The amount of time (in seconds) to wait for the command to complete"))

    @Value("\${odmp.plugin.name}")
    lateinit var pluginName: String

    override fun pluginConfiguration() = PluginConfiguration(
            serviceName = pluginName,
            displayName = "Example Plugin",
            type = ProcessorType.EXTERNAL,
            fields = fields)
}