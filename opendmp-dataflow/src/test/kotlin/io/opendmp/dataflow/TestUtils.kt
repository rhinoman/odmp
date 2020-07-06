package io.opendmp.dataflow

import io.opendmp.dataflow.model.DataflowModel
import io.opendmp.dataflow.model.ProcessorModel
import io.opendmp.dataflow.model.ProcessorType
import org.springframework.context.annotation.PropertySource
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

object TestUtils {

    fun createBasicDataflow(name: String, mongoTemplate: ReactiveMongoTemplate) : DataflowModel {
        val dataflow = DataflowModel(
                name = name,
                creator = "",
                description = "THE $name",
                group = "")

        return mongoTemplate.insert(dataflow).block()!!
    }

    fun createBasicProcessor(name: String,
                             flowId: String,
                             phase: Int,
                             order: Int,
                             type: ProcessorType,
                             mongoTemplate: ReactiveMongoTemplate) : ProcessorModel {
        val proc = ProcessorModel(
                name = name,
                flowId = flowId,
                phase = phase,
                order = order,
                type = ProcessorType.INGEST
        )
        return mongoTemplate.insert(proc).block()!!
    }

}