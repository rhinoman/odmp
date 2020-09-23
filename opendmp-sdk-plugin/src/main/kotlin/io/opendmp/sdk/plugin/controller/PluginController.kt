package io.opendmp.sdk.plugin.controller

import io.opendmp.common.model.plugin.PluginConfiguration
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping

abstract class PluginController(val pluginConfiguration: PluginConfiguration) {

    @GetMapping("/health-check")
    open fun healthCheck(): ResponseEntity<String>{
        return ResponseEntity.ok("Plugin is up!")

    }

    @GetMapping("/configuration")
    open fun getConfiguration(): ResponseEntity<PluginConfiguration> {
        return ResponseEntity.ok(pluginConfiguration)
    }

}