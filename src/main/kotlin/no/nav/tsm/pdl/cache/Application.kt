package no.nav.tsm.pdl.cache

import io.ktor.server.application.*
import no.nav.tsm.pdl.cache.pdl.configurePdlConsumer
import no.nav.tsm.pdl.cache.person.configurePersonRoutes
import no.nav.tsm.pdl.cache.plugins.*

fun Application.module() {
    configureDependencyInjection()
    configureMachineTokenAuth()
    configureSerialization()
    configureMonitoring()
    configureDatabase()

    configurePdlConsumer()
    configurePersonRoutes()
}
