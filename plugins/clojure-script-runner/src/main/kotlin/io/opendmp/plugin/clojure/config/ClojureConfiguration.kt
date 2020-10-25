package io.opendmp.plugin.clojure.config

import io.opendmp.common.model.ProcessorType
import io.opendmp.common.model.plugin.FieldDescription
import io.opendmp.common.model.plugin.FieldType
import io.opendmp.common.model.plugin.PluginConfiguration
import io.opendmp.common.model.properties.ScriptLanguage
import io.opendmp.sdk.plugin.config.PluginConfigurationProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class ClojureConfiguration : PluginConfigurationProvider {

    private val fields = mapOf(
            "language" to FieldDescription(
                    type = FieldType.ENUM,
                    required = true,
                    helperText = "The language to Run",
                    options = ScriptLanguage.values().map{it.toString()}),
            "code" to FieldDescription(
                    type = FieldType.CODE,
                    required = true,
                    helperText = "The code to execute"))

    @Value("\${odmp.plugin.name}")
    lateinit var pluginName: String

    override fun pluginConfiguration() = PluginConfiguration(
            serviceName = pluginName,
            displayName = "Clojure Script Runner",
            type = ProcessorType.SCRIPT,
            fields = fields)
}