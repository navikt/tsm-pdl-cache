package no.nav.tsm.pdl.cache.pdl

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.di.dependencies
import io.opentelemetry.instrumentation.annotations.WithSpan
import no.nav.tsm.ktor.core.SimpleNavn
import no.nav.tsm.ktor.kafka.consumer.KafkaConsumer
import no.nav.tsm.ktor.kafka.consumer.RecordMeta
import no.nav.tsm.ktor.logger
import no.nav.tsm.pdl.Person
import no.nav.tsm.pdl.cache.core.Environment

fun Application.configurePdlConsumer() {
    val env: Environment by dependencies
    val service: PdlPersonConsumerService by dependencies

    install(KafkaConsumer) {
        clientId = env.runtime.name
        groupId = "tsm-pdl-cache-consumer"
        pollDuration = env.pdlConsumer.longPoll
        retryDuration = env.pdlConsumer.retryDelay

        consume<PdlPersonRecord>(
            name = "pdl.pdl-persondokument-v1",
            onTombstone = { service.handleTombstone(it.key) },
            onRecord = { record, meta -> service.handleRecord(record, meta) },
        )
    }
}

class PdlPersonConsumerService(private val pdlPersonService: PdlPersonService) {
    private val logger = logger()

    @WithSpan
    fun handleRecord(record: PdlPersonRecord, meta: RecordMeta) {
        val aktorId = meta.key
        val person = record.let { pdlPerson ->
            if (pdlPerson.hentPerson.foedsel == null && pdlPerson.hentPerson.foedselsdato == null) {
                logger.info(
                    "Received person without foedsel and foedseldato for aktor: $aktorId, offset: ${meta.offset}"
                )
                throw IllegalStateException(
                    "Received person without foedsel and foedseldato for aktor: $aktorId, offset: ${meta.offset}"
                )
            }
            val (isDoed, doedsdato) = getDoedsdato(pdlPerson)
            Person(
                navn = getName(pdlPerson),
                foedselsdato =
                    pdlPerson.hentPerson.foedselsdato?.firstOrNull { !it.metadata.historisk }?.foedselsdato
                        ?: pdlPerson.hentPerson.foedsel?.firstOrNull { !it.metadata.historisk }?.foedselsdato,
                identer = pdlPerson.hentIdenter.identer,
                falskIdent = pdlPerson.hentPerson.falskIdentitet?.erFalsk ?: false,
                doed = isDoed,
                doedsdato = doedsdato,
            )
        }

        pdlPersonService.updatePerson(aktorId, person)
    }

    @WithSpan
    fun handleTombstone(aktorId: String) {
        pdlPersonService.tombstonePerson(aktorId)
    }

    private fun getDoedsdato(pdlPerson: PdlPersonRecord) =
        if (pdlPerson.hentPerson.doedsfall.isNotEmpty()) {
            val pdlDoedsdato = pdlPerson.hentPerson.doedsfall.filter { !it.metadata.historisk && it.doedsdato != null }
            true to
                (pdlDoedsdato.firstOrNull { it.metadata.master == "PDL" }?.doedsdato
                    ?: pdlDoedsdato.firstOrNull()?.doedsdato)
        } else {
            false to null
        }
}

private fun getName(pdlPerson: PdlPersonRecord): SimpleNavn? =
    pdlPerson.hentPerson.navn
        .filter { !it.metadata.historisk }
        .sortedByDescending { it.gyldigFraOgMed }
        .firstOrNull()
        ?.let {
            SimpleNavn(
                fornavn = it.fornavn,
                mellomnavn = it.mellomnavn,
                etternavn = it.etternavn,
            )
        }
