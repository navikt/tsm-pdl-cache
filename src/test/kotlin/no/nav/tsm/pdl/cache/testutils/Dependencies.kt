package no.nav.tsm.pdl.cache.testutils

import io.mockk.mockk
import java.util.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import no.nav.tsm.ktor.nais.RuntimeCluster
import no.nav.tsm.pdl.cache.core.Environment
import no.nav.tsm.pdl.cache.core.KafkaConfig
import no.nav.tsm.pdl.cache.core.KafkaPdlConsumer
import no.nav.tsm.pdl.cache.core.PostgresConfig
import no.nav.tsm.pdl.cache.core.Runtime
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.postgresql.PostgreSQLContainer

fun createIntegrationEnvironment(postgres: PostgreSQLContainer, kafka: ConfluentKafkaContainer? = null) =
    Environment(
        runtime = Runtime(env = RuntimeCluster.LOCAL, name = "test-app"),
        postgres =
            PostgresConfig(
                jdbc = postgres.jdbcUrl,
                username = postgres.username,
                password = postgres.password,
            ),
        kafka =
            KafkaConfig(
                if (kafka != null) Properties().apply { this["bootstrap.servers"] = kafka.bootstrapServers }
                else mockk(),
                pdlConsumer = KafkaPdlConsumer(longPoll = 1000.milliseconds, retryDelay = 1.seconds),
            ),
    )
