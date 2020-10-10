package io.opendmp.sdk.plugin

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object Utils {

    val mapper = jacksonObjectMapper()

    init {
        mapper.findAndRegisterModules()
    }

}