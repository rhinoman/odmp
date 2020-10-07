package io.opendmp.plugin.example

import io.opendmp.sdk.plugin.config.RegistryConfig
import io.opendmp.sdk.plugin.routes.RegisterPlugin
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Import

@SpringBootApplication
@EnableDiscoveryClient
@Import(RegisterPlugin::class, RegistryConfig::class)
class OpendmpPluginExampleApplication

fun main(args: Array<String>) {
    runApplication<OpendmpPluginExampleApplication>(*args)
}
