/*
 * Copyright (c) 2020. James Adam and the Open Data Management Platform contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opendmp.common.util

import io.opendmp.common.exception.CommandExecutionException
import java.io.BufferedInputStream
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.BufferedOutputStream
import java.util.concurrent.TimeUnit

object CommandUtil {

    private val coroutineContext = Dispatchers.IO + SupervisorJob()

    fun streamGobble(bis: BufferedInputStream): ByteArray {
        val bytes = bis.readAllBytes()
        bis.close()
        return bytes
    }

    fun runCommand(command: String, data: ByteArray, timeoutSeconds: Long = 10L): ByteArray {

        val log = LoggerFactory.getLogger(javaClass)
        val proc = ProcessBuilder(command.split(" ")).start()

        // Write the data to stdin async
        CoroutineScope(coroutineContext).launch {
            val outputStream = BufferedOutputStream(proc.outputStream)
            outputStream.write(data)
            outputStream.close()
        }
        // Careful of these two
        val inputBytes = CoroutineScope(coroutineContext).async {
            streamGobble(BufferedInputStream(proc.inputStream))
        }
        val errorBytes = CoroutineScope(coroutineContext).async {
            streamGobble(BufferedInputStream(proc.errorStream))
        }
        //Get the data from the pipe //Wait up to timeoutSeconds for process to complete
        log.info("Waiting up to $timeoutSeconds seconds for $command to finish")
        proc.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        return runBlocking {
            if(proc.exitValue() != 0) {
                val err = errorBytes.await().decodeToString()
                val msg = "Error occurred executing $command: $err"
                throw CommandExecutionException(msg)
            } else {
                inputBytes.await()
            }
        }
    }


}