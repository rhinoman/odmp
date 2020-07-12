package io.opendmp.dataflow.service

import io.opendmp.dataflow.api.request.CreateProcessorRequest
import io.opendmp.dataflow.model.ProcessorModel
import io.opendmp.dataflow.model.SourceModel
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ProcessorService (private val mongoTemplate: ReactiveMongoTemplate) {

    fun createProcessor(data : CreateProcessorRequest,
                        authentication: Authentication) : Mono<ProcessorModel> {
        val inputs: List<SourceModel> = data.inputs?.map {
            SourceModel(
                    sourceType = it.sourceType,
                    sourceId = it.sourceId
            )} ?: mutableListOf()

        val processor = ProcessorModel(
                flowId = data.flowId!!,
                name = data.name!!,
                description = data.description,
                phase = data.phase!!,
                order = data.order!!,
                type = data.type!!,
                inputs = inputs
        )

        return mongoTemplate.save<ProcessorModel>(processor)
    }

}