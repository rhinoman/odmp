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

package io.opendmp.processor.executors

import jep.SharedInterpreter

class PythonExecutor : Executor{
    override fun executeScript(code: String, data: ByteArray): ByteArray {
        val interp = SharedInterpreter()
        interp.exec("from array import array")
        interp.exec(code)
        val output = interp.invoke("process", data)
        return output as ByteArray
    }
}