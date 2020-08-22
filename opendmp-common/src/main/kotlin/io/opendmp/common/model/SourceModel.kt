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

package io.opendmp.common.model

import io.opendmp.common.model.SourceType

/**
 * SourceModel describes the source of a processor input
 * sourceType - determines the type of source,
 *   PROCESSOR for processors that take their input from another
 *   processor.
 * sourceLocation - a string containing the location of the input data
 *   Will be the id of the processor in the case of PROCESSOR source type
 *   Will be the id of the collection in the case of COLLECTION source type
 *   Will be a folder path in the case of INGEST_FILE_DROP, etc.
 * additionalProperties - adapter-specific additional information (usernames, etc.)
 */
open class SourceModel(
        val sourceType: SourceType? = SourceType.NONE,
        val sourceLocation: String? = null,
        val additionalProperties: Map<String, String> = mapOf()) {}