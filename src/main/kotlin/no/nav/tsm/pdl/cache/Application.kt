package no.nav.tsm.pdl.cache

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.plugins.di.dependencies
import no.nav.tsm.pdl.cache.pdl.PdlPersonConsumer
import no.nav.tsm.pdl.cache.person.configurePersonRoutes
import no.nav.tsm.pdl.cache.plugins.configureDatabase
import no.nav.tsm.pdl.cache.plugins.configureDependencyInjection
import no.nav.tsm.pdl.cache.plugins.configureMachineTokenAuth
import no.nav.tsm.pdl.cache.plugins.configureMonitoring
import no.nav.tsm.pdl.cache.plugins.configureSerialization

fun Application.module() {
    configureDependencyInjection()
    configureMachineTokenAuth()
    configureSerialization()
    configureMonitoring()
    configureDatabase()

    configurePersonRoutes()

    val consumer: PdlPersonConsumer by dependencies
    monitor.subscribe(ApplicationStarted) {
        with(consumer) { consume() }
    }
}
