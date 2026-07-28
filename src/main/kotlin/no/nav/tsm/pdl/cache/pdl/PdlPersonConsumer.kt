package no.nav.tsm.pdl.cache.pdl

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.server.application.Application
import io.opentelemetry.instrumentation.annotations.WithSpan
import java.time.Duration
import java.util.Properties
import kotlin.collections.set
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.toJavaDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nav.tsm.ktor.logger
import no.nav.tsm.pdl.cache.testutils.Environment
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer

class PdlPersonConsumer(
    environment: Environment,
    private val pdlPersonService: PdlPersonService,
) {
    private val logger = logger()
    private val kafkaConfig = environment.kafka

    private val topicName = "pdl.pdl-persondokument-v1"
    private val groupId = "tsm-pdl-cache-consumer"

    private val duration: Duration = environment.kafka.pdlConsumer.longPoll.toJavaDuration()
    private val consumer: KafkaConsumer<String, ByteArray?>

    init {
        val kafkaProperties = Properties()

        kafkaProperties.apply {
            environment.kafka.config.forEach { (key, value) -> this[key] = value }
            this[ConsumerConfig.GROUP_ID_CONFIG] = groupId
            this[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
            this[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "true"
        }

        consumer = KafkaConsumer(kafkaProperties, StringDeserializer(), ByteArrayDeserializer())
    }

    fun Application.consume() {
        launch(Dispatchers.IO) {
            subscribe()
            try {
                while (isActive) {
                    try {
                        val records = consumer.poll(duration)
                        if (records.isEmpty) continue

                        logger.info("PDL consumer polled ${records.count()} records from $topicName")

                        for (record in records) {
                            handleRecord(record)
                        }
                    } catch (_: CancellationException) {
                        logger.info("Consumer cancelled")
                    } catch (e: Exception) {
                        logger.error(
                            "Error processing messages from kafka delaying ${kafkaConfig.pdlConsumer.retryDelay} to try again",
                            e,
                        )
                        unsubscribe()
                        delay(kafkaConfig.pdlConsumer.retryDelay)
                        subscribe()
                    }
                }
            } finally {
                withContext(NonCancellable) { consumer.unsubscribe() }
            }
        }
    }

    fun subscribe() {
        logger.info("Subscribing $topicName")
        consumer.subscribe(listOf(topicName))
    }

    fun unsubscribe() {
        logger.info("Unsubscribing $topicName")
        consumer.unsubscribe()
    }

    @WithSpan
    private fun handleRecord(record: ConsumerRecord<String, ByteArray?>) {
        val aktorId = record.key()
        val pdlPerson = record.value()?.let { pdlObjectMapper.readValue<PdlPerson>(it) }
        if (pdlPerson == null) {
            pdlPersonService.tombstonePerson(aktorId)
            return
        }

        val person = pdlPerson.let { pdlPerson ->
            if (pdlPerson.hentPerson.foedsel == null && pdlPerson.hentPerson.foedselsdato == null) {
                logger.info(
                    "Received person without foedsel and foedseldato for aktor: $aktorId, offset: ${record.offset()}"
                )
                throw IllegalStateException(
                    "Received person without foedsel and foedseldato for aktor: $aktorId, offset: ${record.offset()}"
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

    private fun getDoedsdato(pdlPerson: PdlPerson) =
        if (pdlPerson.hentPerson.doedsfall.isNotEmpty()) {
            val pdlDoedsdato = pdlPerson.hentPerson.doedsfall.filter { !it.metadata.historisk && it.doedsdato != null }
            true to
                (pdlDoedsdato.firstOrNull { it.metadata.master == "PDL" }?.doedsdato
                    ?: pdlDoedsdato.firstOrNull()?.doedsdato)
        } else {
            false to null
        }

    private val pdlObjectMapper: ObjectMapper =
        jacksonObjectMapper().apply {
            registerModule(JavaTimeModule())
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        }
}

private fun getName(pdlPerson: PdlPerson): Navn? =
    pdlPerson.hentPerson.navn
        .filter { !it.metadata.historisk }
        .sortedByDescending { it.gyldigFraOgMed }
        .firstOrNull()
        ?.let {
            Navn(
                fornavn = it.fornavn,
                mellomnavn = it.mellomnavn,
                etternavn = it.etternavn,
            )
        }
