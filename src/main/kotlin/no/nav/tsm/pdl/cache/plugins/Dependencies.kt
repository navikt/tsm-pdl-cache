package no.nav.tsm.pdl.cache.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import no.nav.tsm.pdl.cache.core.Environment
import no.nav.tsm.pdl.cache.core.initializeEnvironment
import no.nav.tsm.pdl.cache.pdl.PdlPersonConsumer
import no.nav.tsm.pdl.cache.pdl.PdlPersonService
import no.nav.tsm.pdl.cache.pdl.PersonRepository
import no.nav.tsm.pdl.cache.person.PersonService

fun Application.configureDependencyInjection() {
    val config = environment.config

    dependencies {
        provide<Environment> { initializeEnvironment(config) }
        provide(PdlPersonConsumer::class)
        provide(PdlPersonService::class)
        provide(PersonRepository::class)
        provide(PersonService::class)
    }
}
