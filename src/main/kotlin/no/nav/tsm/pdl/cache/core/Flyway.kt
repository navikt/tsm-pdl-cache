package no.nav.tsm.pdl.cache.core

import org.flywaydb.core.Flyway

fun getFlyway(postgresConfig: PostgresConfig): Flyway =
    Flyway.configure()
        .dataSource(postgresConfig.jdbc, postgresConfig.username, postgresConfig.password)
        .locations("db/migration")
        .load()
