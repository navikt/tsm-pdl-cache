package no.nav.tsm.pdl.cache.pdl

import com.fasterxml.jackson.module.kotlin.readValue
import io.opentelemetry.instrumentation.annotations.WithSpan
import no.nav.tsm.pdl.cache.util.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class PdlNameUpdateConsumer(val pdlPersonService: PdlPersonService) {

    private val logger = LoggerFactory.getLogger(PdlNameUpdateConsumer::class.java)

    @WithSpan
    @KafkaListener(id = "tsm-pdl-cache-consumer-name-udpate", topics = ["pdl.pdl-persondokument-v1"])
    fun consume(record: List<ConsumerRecord<String, String?>>) {

        val recordsToUpdate = record.mapNotNull { consumerRecord ->
            consumerRecord.value()?.let {  consumerRecord.key() to objectMapper.readValue<PdlPerson>(it) }
        }.filter {
            it.second.hentPerson.navn.filter { navn -> !navn.metadata.historisk }.size > 1
        }

        recordsToUpdate.forEach { record ->
            logger.info("updating name for person with aktorid ${record.first}")
            val persons = pdlPersonService.personRepository.getPerson(record.first)
            when (persons.size) {
                0 -> logger.info("Person with aktorid ${record.first} not found")
                1 -> {
                    logger.info("Person with aktorid ${record.first} found, updating")
                    val person = persons.single()
                    if(person.navn == null) {
                        pdlPersonService.personRepository.updateName(record.first, getName(record.second) ?: throw IllegalStateException("must have a name"))
                    } else {
                        logger.info("Person with aktorid ${record.first} has name no need to update")
                    }
                }
                else -> logger.info("Person with aktorid ${record.first} found ${persons.size} persons, not updating")
            }
        }
    }
}
