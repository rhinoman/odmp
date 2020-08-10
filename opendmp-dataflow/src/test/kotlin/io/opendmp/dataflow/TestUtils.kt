package io.opendmp.dataflow

import io.opendmp.common.model.ProcessorType
import io.opendmp.common.model.SourceModel
import io.opendmp.common.model.SourceType
import io.opendmp.common.model.properties.DestinationType
import io.opendmp.dataflow.model.*
import org.springframework.context.annotation.PropertySource
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalUnit
import java.util.*
import java.util.concurrent.TimeUnit

object TestUtils {

    fun createBasicDataflow(name: String) : DataflowModel {
        return DataflowModel(
                name = name,
                creator = "",
                description = "THE $name",
                group = "")
    }

    fun createBasicProcessor(name: String,
                             flowId: String,
                             phase: Int,
                             order: Int,
                             type: ProcessorType) : ProcessorModel {
        return ProcessorModel(
                name = name,
                flowId = flowId,
                phase = phase,
                order = order,
                type = ProcessorType.INGEST,
                triggerType = TriggerType.AUTOMATIC,
                creator = "test_user",
                inputs = mutableListOf(SourceModel(sourceType = SourceType.PROCESSOR, sourceLocation = ""))
        )
    }

    private val fmt = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault())

    fun createBasicDataset(collectionId: String, time: Instant) : DatasetModel {
        return DatasetModel(
                collectionId = collectionId,
                createdOn = time,
                dataflowId = UUID.randomUUID().toString(),
                destinationType = DestinationType.FOLDER,
                location = "/tmp/out",
                name = "TheDataflow-${fmt.format(time)}")
    }

    fun createBasicDatasets(collectionId: String, num: Int) : List<DatasetModel> {
        return (1..num).map {
            val time = Instant.now().plusSeconds(it.toLong())
            createBasicDataset(collectionId, time)
        }
    }

    fun createBasicCollection(name: String) : CollectionModel {
        return CollectionModel(
                creator = UUID.randomUUID().toString(),
                group = "",
                name = name
        )
    }

}