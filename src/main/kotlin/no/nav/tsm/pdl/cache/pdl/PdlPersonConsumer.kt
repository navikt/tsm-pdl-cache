package no.nav.tsm.pdl.cache.pdl

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tsm.pdl.cache.util.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class PdlPersonConsumer(val pdlPersonService: PdlPersonService) {

    private val logger = LoggerFactory.getLogger(PdlPersonConsumer::class.java)

    @KafkaListener(id = "pdl-person-consumer", topics = ["pdl-person-topic"])
    fun consume(record: ConsumerRecord<String, String?>) {
        val aktorId = record.key()
        val person = record.value()?.let { objectMapper.readValue<Person>(it) }
        pdlPersonService.updatePerson(aktorId, person)
    }
}
