package io.opendmp.plugin.ffmpeg.process

import io.opendmp.sdk.plugin.process.OdmpProcessor
import org.apache.camel.Exchange
import org.springframework.stereotype.Component

@Component
class FFMPEGProcessor : OdmpProcessor {

    override fun process(data:ByteArray): ByteArray {
        return "FOOBAR".toByteArray(Charsets.UTF_8)
    }


}