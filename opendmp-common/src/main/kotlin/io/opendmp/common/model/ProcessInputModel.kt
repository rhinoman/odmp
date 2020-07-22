/*
 * Copyright (c) 2020. The Open Data Management Platform contributors.
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

/**
 * Two additional fields needed beyond the SourceModel
 * dataLocationType - CACHE, DISK, or NONE - depending on where the input data has been stored
 * locationKey - either the CACHE key (e.g., in REDIS) or the file location (or S3 key)
 *
 * For ingest processors, dataLocationType will be NONE and locationKey will be null
 */
class ProcessInputModel(sourceType: SourceType,
                        sourceLocation: String? = null,
                        val dataLocationType: DataLocationType = DataLocationType.NONE,
                        val locationKey: String? = null) :
    SourceModel(sourceType = sourceType, sourceLocation = sourceLocation) {
}