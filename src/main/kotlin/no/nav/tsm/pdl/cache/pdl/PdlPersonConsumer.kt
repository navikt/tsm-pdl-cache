package no.nav.tsm.pdl.cache.pdl

import com.fasterxml.jackson.module.kotlin.readValue
import io.opentelemetry.instrumentation.annotations.WithSpan
import no.nav.tsm.pdl.cache.util.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class PdlPersonConsumer(val pdlPersonService: PdlPersonService) {

    private val logger = LoggerFactory.getLogger(PdlPersonConsumer::class.java)

    @WithSpan
    @KafkaListener(id = "tsm-pdl-cache-consumer", topics = ["pdl.pdl-persondokument-v1"])
    fun consume(record: ConsumerRecord<String, String?>) {
        val aktorId = record.key()
        val person = record.value()
            ?.let { objectMapper.readValue<PdlPerson>(it) }
            ?.let { pdlPerson ->
                Person(
                    navn = pdlPerson.hentPerson.navn.singleOrNull { !it.metadata.historisk }?.let {
                        Navn(
                            fornavn = it.fornavn,
                            mellomnavn = it.mellomnavn,
                            etternavn = it.etternavn
                        )
                    },
                    foedselsdato = pdlPerson.hentPerson.foedselsdato.singleOrNull { !it.metadata.historisk }?.foedselsdato,
                    identer = pdlPerson.hentIdenter.identer
                )
            }
        if(person?.navn?.fornavn == null) {
            logger.info("Received person without name for aktor: $aktorId, offset: ${record.offset()}")
        }
        pdlPersonService.updatePerson(aktorId, person)
    }
}
