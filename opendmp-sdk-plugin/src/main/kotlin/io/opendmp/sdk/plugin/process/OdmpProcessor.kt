package io.opendmp.sdk.plugin.process

interface OdmpProcessor {
    fun process(data: ByteArray): ByteArray
}