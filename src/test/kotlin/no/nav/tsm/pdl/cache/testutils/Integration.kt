package no.nav.tsm.pdl.cache.testutils

import java.util.Properties
import kotlin.collections.set
import kotlin.io.use
import kotlin.use
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.postgresql.PostgreSQLContainer

abstract class WithPostgresql {
    companion object {
        val postgres = PostgreSQLContainer("postgres:17-alpine").apply { start() }
        val config = createIntegrationEnvironment(postgres, null)

        fun runMigrations(clean: Boolean = false) {
            val flyway =
                Flyway.configure()
                    .dataSource(
                        config.postgres.jdbc,
                        config.postgres.username,
                        config.postgres.password,
                    )
                    .cleanDisabled(false)
                    .createSchemas(true)
                    .locations("db/migration")
                    .load()

            if (clean) {
                flyway.clean()
            }
            flyway.migrate()
        }

        fun connect() {
            Database.connect(
                url = config.postgres.jdbc,
                user = config.postgres.username,
                password = config.postgres.password,
            )
        }
    }
}

abstract class WithPostgresAndKafka : WithPostgresql() {
    companion object {
        private val topics = listOf("pdl.pdl-persondokument-v1")

        val kafka =
            ConfluentKafkaContainer("confluentinc/cp-kafka:8.1.0").apply {
                start()
                createTopics()
            }

        private fun ConfluentKafkaContainer.createTopics() {
            val props =
                Properties().apply {
                    this[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
                }
            AdminClient.create(props).use { admin ->
                admin.createTopics(topics.map { NewTopic(it, 1, 1) }).all().get()
            }
        }

        fun recreateTopics() {
            val props =
                Properties().apply {
                    this[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = kafka.bootstrapServers
                }

            AdminClient.create(props).use { admin ->
                admin.deleteTopics(topics).all().get()
                admin.createTopics(topics.map { NewTopic(it, 1, 1) }).all().get()
            }
        }
    }

    suspend fun produce(topic: String, key: String, value: ByteArray?) {
        withContext(Dispatchers.IO) {
            val props = Properties().apply { this["bootstrap.servers"] = kafka.bootstrapServers }
            KafkaProducer(props, StringSerializer(), ByteArraySerializer()).use { producer ->
                producer.send(ProducerRecord(topic, key, value)).get()
            }
        }
    }
}
