package io.opendmp.plugin.ffmpeg.controller

import io.opendmp.plugin.ffmpeg.config.FFMPEGConfiguration
import io.opendmp.sdk.plugin.controller.PluginController

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class FfmpegController : PluginController(FFMPEGConfiguration.pluginConfiguration) {}