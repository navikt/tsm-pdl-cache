package no.nav.tsm.pdl.cache.person

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tsm.ktor.auth.entra.entraMachineToken
import no.nav.tsm.pdl.cache.person.exceptions.PersonNotFoundException
import no.nav.tsm.pdl.cache.person.exceptions.TooManyPersonException

fun Application.configurePersonRoutes() {
    val personService: PersonService by dependencies

    routing {
        entraMachineToken {
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
