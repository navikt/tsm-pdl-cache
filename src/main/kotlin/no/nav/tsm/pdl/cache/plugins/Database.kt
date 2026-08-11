package no.nav.tsm.pdl.cache.plugins

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import no.nav.tsm.pdl.cache.core.Environment
import no.nav.tsm.pdl.cache.core.getFlyway
import org.jetbrains.exposed.v1.jdbc.Database

fun Application.configureDatabase() {
    val env: Environment by dependencies

    getFlyway(env.postgres).migrate()

    Database.connect(
        url = env.postgres.jdbc,
        user = env.postgres.username,
        password = env.postgres.password,
    )
}
