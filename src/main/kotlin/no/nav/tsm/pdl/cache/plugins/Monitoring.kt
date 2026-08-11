package no.nav.tsm.pdl.cache.plugins

import io.ktor.server.application.*
import no.nav.tsm.ktor.nais.NaisMonitoring
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun Application.configureMonitoring() {
    install(NaisMonitoring) {
        ready {
            check("database ready") {
                try {
                    transaction { exec("SELECT 1") }
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }
}
