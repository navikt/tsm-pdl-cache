package no.nav.tsm.pdl.cache.person

import io.kotest.matchers.equals.shouldEqual
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson3.jackson
import io.ktor.server.plugins.di.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlin.test.Test
import no.nav.tsm.pdl.cache.pdl.IDENT_GRUPPE
import no.nav.tsm.pdl.cache.pdl.Ident
import no.nav.tsm.pdl.cache.pdl.Navn
import no.nav.tsm.pdl.cache.pdl.Person
import no.nav.tsm.pdl.cache.person.exceptions.PersonNotFoundException
import no.nav.tsm.pdl.cache.person.exceptions.TooManyPersonException
import no.nav.tsm.pdl.cache.plugins.configureMachineTokenAuth
import no.nav.tsm.pdl.cache.plugins.configureSerialization

class PersonApiTest {

    val personService = mockk<PersonService>()

    private fun ApplicationTestBuilder.configureRoutes(): HttpClient {
        application {
            dependencies { provide { personService } }
            configureSerialization()
            configureMachineTokenAuth()
            configurePersonRoutes()
        }

        return testHttpClient()
    }

    @Test
    fun `should get 404 when service throws not found`() = testApplication {
        val client = configureRoutes()

        every { personService.getPerson("123") } throws PersonNotFoundException("Person not found")

        val response = client.get("/api/person", { header("ident", "123") })

        response.status shouldEqual HttpStatusCode.NotFound
    }

    @Test
    fun `should get 409 when service throws too many persons`() = testApplication {
        val client = configureRoutes()

        every { personService.getPerson("123") } throws TooManyPersonException("Person not found")

        val response = client.get("/api/person", { header("ident", "123") })

        response.status shouldEqual HttpStatusCode.Conflict
    }

    @Test
    fun `should get 200 when service returns person`() = testApplication {
        val client = configureRoutes()

        every { personService.getPerson("123") } returns
            Person(
                navn = Navn("Fornavn", "Mellomnavn", "Etternavn"),
                foedselsdato = LocalDate.of(1991, 1, 1),
                identer =
                    listOf(
                        Ident("aktorId", IDENT_GRUPPE.AKTORID, false),
                        Ident("123", IDENT_GRUPPE.FOLKEREGISTERIDENT, false),
                        Ident("321", IDENT_GRUPPE.FOLKEREGISTERIDENT, true),
                        Ident("npid", IDENT_GRUPPE.NPID, false),
                    ),
                falskIdent = false,
                doedsdato = null,
                doed = false,
            )

        val response =
            client.get("/api/person") {
                contentType(ContentType.Application.Json)
                header("ident", "123")
            }

        response.status shouldEqual HttpStatusCode.OK
        val person = response.body<Person>()
        person.navn?.fornavn shouldEqual "Fornavn"
        person.foedselsdato shouldEqual LocalDate.of(1991, 1, 1)
        person.identer.size shouldEqual 4
    }
}

private fun ApplicationTestBuilder.testHttpClient(): HttpClient {
    return createClient {
        install(ContentNegotiation) {
            jackson {}
        }
    }
}
