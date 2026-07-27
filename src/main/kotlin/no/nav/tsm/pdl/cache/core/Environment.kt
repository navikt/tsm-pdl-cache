package no.nav.tsm.pdl.cache.testutils

import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.getAs
import java.util.Properties
import kotlin.time.Duration
import no.nav.tsm.ktor.nais.RuntimeCluster
import no.nav.tsm.ktor.nais.getRuntimeCluster

class Runtime(
    val env: RuntimeCluster,
    val name: String,
)

class KafkaPdlConsumer(
    val longPoll: Duration,
    val retryDelay: Duration,
)

class KafkaConfig(val config: Properties, val pdlConsumer: KafkaPdlConsumer)

class PostgresConfig(
    val jdbc: String,
    val username: String,
    val password: String,
)

class EntraAuth(val issuer: String, val jwksUri: String, val audience: String)

class Environment(
    val runtime: Runtime,
    val kafka: KafkaConfig,
    val postgres: PostgresConfig,
    val auth: () -> EntraAuth,
)

fun initializeEnvironment(config: ApplicationConfig): Environment {
    val kafkaProperties =
        KafkaConfig(
            config =
                Properties().apply {
                    config.config("kafka.config").toMap().forEach { this[it.key] = it.value }
                },
            pdlConsumer =
                KafkaPdlConsumer(
                    longPoll = config.property("kafka.pdlConsumer.longPoll").getAs(),
                    retryDelay = config.property("kafka.pdlConsumer.retryDelay").getAs(),
                ),
        )

    return Environment(
        kafka = kafkaProperties,
        runtime =
            Runtime(
                env = getRuntimeCluster(),
                name = config.property("app.name").getString(),
            ),
        postgres =
            PostgresConfig(
                jdbc = config.property("postgres.jdbc").getString(),
                username = config.property("postgres.username").getString(),
                password = config.property("postgres.password").getString(),
            ),
        auth = {
            EntraAuth(
                audience = config.property("auth.entra.audience").getString(),
                jwksUri = config.property("auth.entra.openid.jwks").getString(),
                issuer = config.property("auth.entra.openid.issuer").getString(),
            )
        },
    )
}
