package io.opendmp.sdk.plugin.config

import io.opendmp.common.model.plugin.PluginConfiguration

interface PluginConfigurationProvider {

    fun pluginConfiguration(): PluginConfiguration

}