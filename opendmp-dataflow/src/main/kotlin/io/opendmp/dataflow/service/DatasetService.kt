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

package io.opendmp.dataflow.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.google.common.net.HttpHeaders
import com.mongodb.client.result.DeleteResult
import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import io.opendmp.common.exception.CollectProcessorException
import io.opendmp.common.message.CollectionCompleteMessage
import io.opendmp.common.model.properties.DestinationType
import io.opendmp.common.util.MessageUtil.mapper
import io.opendmp.dataflow.api.exception.BadRequestException
import io.opendmp.dataflow.api.exception.PermissionDeniedException
import io.opendmp.dataflow.api.response.DatasetDetail
import io.opendmp.dataflow.api.response.DownloadRequestResponse
import io.opendmp.dataflow.api.response.ProcessorDetail
import io.opendmp.dataflow.model.CollectionModel
import io.opendmp.dataflow.model.DataflowModel
import io.opendmp.dataflow.model.DatasetModel
import org.apache.tika.Tika
import org.apache.tika.io.TikaInputStream
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.remove
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.webjars.NotFoundException
import reactor.core.Disposable
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class DatasetService (private val mongoTemplate: ReactiveMongoTemplate,
                      private val dataflowService: DataflowService,
                      private val collectionService: CollectionService,
                      private val s3Client: AmazonS3,
                      private val esClient: RestHighLevelClient) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${odmp.file.base-path}")
    lateinit var basePath: String

    @Value("\${odmp.secret}")
    lateinit var secret: String

    fun createDataset(msg: CollectionCompleteMessage): Disposable {
        val collection = collectionService.get(msg.collectionId)
                .switchIfEmpty { throw CollectProcessorException("the specified collection does not exist.") }
        val dataflow = dataflowService.get(msg.flowId)
                .switchIfEmpty { throw CollectProcessorException("the specified dataflow does not exist") }

       return Mono.zip(collection, dataflow).subscribe { xs ->
           val flowName = xs.t2.name
           val time = msg.timeStamp
           val fmt = DateTimeFormatter
                   .ofPattern("yyyyDDDHHmmss.S")
                   .withZone(ZoneId.systemDefault())
           val prefix = msg.prefix
           val name = if(prefix != null) {
               "$prefix-${fmt.format(time)}"
           } else {
               "$flowName-${fmt.format(time)}"
           }
           val dataset = DatasetModel(
                   name = name,
                   collectionId = msg.collectionId,
                   dataflowId = msg.flowId,
                   destinationType = msg.destinationType,
                   location = msg.location,
                   dataTag = msg.dataTag,
                   history = msg.history,
                   createdOn = msg.timeStamp)
           mongoTemplate.save(dataset).block()
        }
    }

    fun get(id: String) : Mono<DatasetModel> {
        return mongoTemplate.findById(id)
    }

    fun getDetail(id: String) : Mono<DatasetDetail> {
        return get(id).flatMap { ds ->
            mongoTemplate.findById<CollectionModel>(ds.collectionId).flatMap<DatasetDetail> { coll ->
                Mono.just(DatasetDetail(dataset = ds, collection = coll))
            }
        }
    }

    fun delete(id: String) : Mono<DeleteResult> {
        val query = Query(Criteria.where("id").isEqualTo(id))
        return mongoTemplate.remove<DatasetModel>(query)
    }

    fun requestDownload(id: String) : Mono<DownloadRequestResponse> {
        return get(id).map {ds ->
            when(ds.destinationType) {
                DestinationType.FOLDER -> requestDownloadToken(ds)
                DestinationType.S3 -> requestDownloadLink(ds)
                DestinationType.ELASTIC_SEARCH -> requestDownloadToken(ds)
                DestinationType.NONE -> requestDownloadToken(ds)
            }
        }
    }

    /**
     * Generates a temporarily valid download token using a shared secret
     */
    fun requestDownloadToken(ds: DatasetModel) : DownloadRequestResponse {
        val timestamp = Instant.now()
        val payload = mapOf("datasetId" to ds.id, "timestamp" to timestamp)
        val signer: JWSSigner = MACSigner(secret)
        val tok = JWSObject(JWSHeader(JWSAlgorithm.HS256), Payload(mapper.writeValueAsString(payload)))
        tok.sign(signer)
        return DownloadRequestResponse(token = tok.serialize(), url = null, timestamp = timestamp)
    }

    /**
     * Generates a pre-signed URL for S3-compatible Object storage
     */
    fun requestDownloadLink(ds: DatasetModel) : DownloadRequestResponse {
        val timestamp = Instant.now()
        val locc = ds.location.split(":")
        val bucket = locc[0]
        val key = locc[1]
        val exp = Instant.now().plusSeconds(10L)
        val url = s3Client.generatePresignedUrl(bucket, key, Date.from(exp))
        return DownloadRequestResponse(token = null, url = url.toString(), timestamp = timestamp)
    }

    fun download(token: String) : Mono<ResponseEntity<InputStreamResource>> {
        val jws = JWSObject.parse(token)
        val verifier: JWSVerifier = MACVerifier(secret)
        if(!jws.verify(verifier)) {
            throw PermissionDeniedException("Download Token is invalid")
        }
        val req = mapper.readValue(jws.payload.toString(), Map::class.java)
        val id = req["datasetId"] as String
        val timestamp = mapper.convertValue(req["timestamp"], Instant::class.java)
        // If the token is more than 5 seconds old, reject
        if(Instant.now().minusMillis(5000).isAfter(timestamp)) {
            throw PermissionDeniedException("Token is expired")
        }
        val entity = ResponseEntity.ok().header(HttpHeaders.X_FRAME_OPTIONS, "SAMEORIGIN")
        return get(id).switchIfEmpty { throw NotFoundException("Requested data not found") }
                .map { ds ->
                    when (ds.destinationType) {
                        DestinationType.FOLDER -> {
                            val filePath = Path.of(basePath).resolve(ds.location)
                            val tika = Tika()
                            val inputStream = TikaInputStream.get(filePath)
                            val mimeType = tika.detect(inputStream)
                            entity.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"${ds.name}\"")
                                    .header(HttpHeaders.CONTENT_TYPE, mimeType)
                                    .body(InputStreamResource(inputStream))
                        }
                        DestinationType.ELASTIC_SEARCH -> {
                            val locc = ds.location.split(":")
                            val index = locc[0]
                            val recordId = locc[1]
                            val resp: GetResponse = esClient.get(GetRequest(index, recordId), RequestOptions.DEFAULT)

                            entity.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"${ds.name}.json\"")
                                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                    .body(InputStreamResource(ByteArrayInputStream(resp.sourceAsBytes)))
                        }
                        DestinationType.NONE -> {
                            entity.body(InputStreamResource(InputStream.nullInputStream()))
                        }
                        else -> throw RuntimeException ("Unknown Destination Type")
                    }
                }
    }

}