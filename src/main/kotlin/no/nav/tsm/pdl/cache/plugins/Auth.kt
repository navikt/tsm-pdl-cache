package no.nav.tsm.pdl.cache.plugins

import io.ktor.server.application.*
import no.nav.tsm.ktor.auth.entra.EntraAuth

fun Application.configureMachineTokenAuth() {
    install(EntraAuth) {
        autoStub = true
    }
}
