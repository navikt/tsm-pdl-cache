package no.nav.tsm.pdl.cache.testutils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.tsm.ktor.kafka.test.KafkaContainer
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerRecord
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
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
        val topics = listOf("pdl.pdl-persondokument-v1")
        val kafka = KafkaContainer(createTopics = topics)
        val producer = kafka.createAnythingProducer()

        fun recreateTopics() {
            AdminClient.create(kafka.config).use { admin ->
                admin.deleteTopics(topics).all().get()
                admin.createTopics(topics.map { NewTopic(it, 1, 1) }).all().get()
            }
        }
    }

    suspend fun produce(topic: String, key: String, value: ByteArray?) {
        withContext(Dispatchers.IO) {
            producer.send(ProducerRecord(topic, key, value)).get()
        }
    }
}
