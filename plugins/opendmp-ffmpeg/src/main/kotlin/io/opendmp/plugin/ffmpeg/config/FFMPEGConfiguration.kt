package io.opendmp.plugin.ffmpeg.config

import io.opendmp.common.model.ProcessorType
import io.opendmp.common.model.plugin.FieldType
import io.opendmp.common.model.plugin.PluginConfiguration

object FFMPEGConfiguration {

    private val fields = mapOf(
            "command" to FieldType.STRING,
            "timeout" to FieldType.NUMBER)

    val pluginConfiguration = PluginConfiguration(
            name = "FFMPEG Processor",
            type = ProcessorType.EXTERNAL,
            fields = fields)
}