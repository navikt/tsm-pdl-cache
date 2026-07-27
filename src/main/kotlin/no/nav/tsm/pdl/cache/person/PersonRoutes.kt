package no.nav.tsm.pdl.cache.person

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import no.nav.tsm.pdl.cache.person.exceptions.PersonNotFoundException
import no.nav.tsm.pdl.cache.person.exceptions.TooManyPersonException
import no.nav.tsm.pdl.cache.plugins.ENTRA_MACHINE_TOKEN

fun Application.configurePersonRoutes() {
    val personService: PersonService by dependencies

    routing {
        authenticate(ENTRA_MACHINE_TOKEN) {
            get("/api/person") {
                val ident = call.request.headers["ident"]
                if (ident.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Missing ident header")
                    return@get
                }

                try {
                    val person = personService.getPerson(ident)
                    call.respond(person)
                } catch (ex: TooManyPersonException) {
                    call.respond(HttpStatusCode.Conflict, ex.message ?: "Unknown error")
                } catch (ex: PersonNotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ex.message ?: "Unknown error")
                }
            }
        }
    }
}
