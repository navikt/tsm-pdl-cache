package no.nav.tsm.pdl.cache.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.di.dependencies
import java.net.URI
import no.nav.tsm.ktor.nais.RuntimeCluster
import no.nav.tsm.pdl.cache.testutils.Environment

const val ENTRA_MACHINE_TOKEN = "internal-entra-m2m"

fun Application.configureMachineTokenAuth() {
    val env: Environment by dependencies

    if (env.runtime.env == RuntimeCluster.LOCAL) {
        configureLocalMachineTokenAuth()
        return
    }

    val entra = env.auth()
    val jwkProvider = JwkProviderBuilder(URI(entra.jwksUri).toURL()).build()

    authentication {
        jwt(ENTRA_MACHINE_TOKEN) {
            verifier(jwkProvider, entra.issuer) { withAudience(entra.audience) }
            validate { credentials -> JWTPrincipal(credentials.payload) }
        }
    }
}

fun Application.configureLocalMachineTokenAuth() {
    authentication { provider(ENTRA_MACHINE_TOKEN) { authenticate {} } }
}
