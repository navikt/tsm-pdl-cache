package no.nav.tsm.pdl.cache.testutils

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
        val kafka = ConfluentKafkaContainer("confluentinc/cp-kafka:8.1.0").apply { start() }
    }
}
