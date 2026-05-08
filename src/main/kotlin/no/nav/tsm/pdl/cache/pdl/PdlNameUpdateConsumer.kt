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
    @KafkaListener(id = "tsm-pdl-cache-consumer-name-udpate-2", topics = ["pdl.pdl-persondokument-v1"])
    fun consume(record: ConsumerRecord<String, String?>) {

        val pdlPerson =  record.value()?.let { objectMapper.readValue<PdlPerson>(it) }

        if(pdlPerson == null) {
            return
        }

        if (pdlPerson.hentPerson.navn.filter { navn -> !navn.metadata.historisk }.size < 2) {
            return
        }

        logger.info("updating name for person with aktorid ${record.key()}")
        val persons = pdlPersonService.personRepository.getPerson(record.key())
        when (persons.size) {
            0 -> logger.info("Person with aktorid ${record.key()} not found")
            1 -> {
                logger.info("Person with aktorid ${record.key()} found, updating")
                val person = persons.single()
                if(person.navn == null) {
                    pdlPersonService.personRepository.updateName(record.key(), getName(pdlPerson) ?: throw IllegalStateException("must have a name"))
                } else {
                    logger.info("Person with aktorid ${record.key()} has name no need to update")
                }
            }
            else -> logger.info("Person with aktorid ${record.key()} found ${persons.size} persons, not updating")
            }

    }
}
