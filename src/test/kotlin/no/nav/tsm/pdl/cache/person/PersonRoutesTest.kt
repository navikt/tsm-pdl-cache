package no.nav.tsm.pdl.cache.person

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.equals.shouldEqual
import io.ktor.client.HttpClient
import io.ktor.server.plugins.di.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlin.test.Test
import no.nav.tsm.ktor.auth.texas.Texas
import no.nav.tsm.ktor.core.SimpleNavn
import no.nav.tsm.pdl.Ident
import no.nav.tsm.pdl.IdentGruppe
import no.nav.tsm.pdl.PdlClient
import no.nav.tsm.pdl.PdlCloudClient
import no.nav.tsm.pdl.PdlCloudConfig
import no.nav.tsm.pdl.Person
import no.nav.tsm.pdl.cache.person.exceptions.PersonNotFoundException
import no.nav.tsm.pdl.cache.person.exceptions.TooManyPersonException
import no.nav.tsm.pdl.cache.plugins.configureMachineTokenAuth
import no.nav.tsm.pdl.cache.plugins.configureSerialization

class PersonRoutesTest {
    val personService = mockk<PersonService>()
    val texas = mockk<Texas>(relaxed = true)

    private suspend fun ApplicationTestBuilder.configureRoutes(): PdlClient {
        val client = createClient {}

        application {
            dependencies {
                provide<HttpClient> { client }
                provide<PersonService> { personService }
                provide<Texas> { texas }
                provide<PdlCloudConfig> { PdlCloudConfig(url = "") }
                provide<PdlClient>(PdlCloudClient::class)
            }
            configureSerialization()
            configureMachineTokenAuth()
            configurePersonRoutes()
        }

        startApplication()

        val pdlClient: PdlClient by application.dependencies
        return pdlClient
    }

    @Test
    fun `should return null person is not found`() = testApplication {
        val client = configureRoutes()
        every { personService.getPerson("123") } throws PersonNotFoundException("Person not found")

        val response = client.getPerson("123")

        response shouldEqual null
    }

    @Test
    fun `should get unknown error when service throws too many persons`() = testApplication {
        val client = configureRoutes()
        every { personService.getPerson("123") } throws TooManyPersonException("Person not found")

        shouldThrow<PdlClient.UnknownError> {
            client.getPerson("123")
        }
    }

    @Test
    fun `should get 200 when service returns person`() = testApplication {
        val client = configureRoutes()
        every { personService.getPerson("123") } returns
            Person(
                navn = SimpleNavn("Fornavn", "Mellomnavn", "Etternavn"),
                foedselsdato = LocalDate.of(1991, 1, 1),
                identer =
                    listOf(
                        Ident("aktorId", IdentGruppe.AKTORID, false),
                        Ident("123", IdentGruppe.FOLKEREGISTERIDENT, false),
                        Ident("321", IdentGruppe.FOLKEREGISTERIDENT, true),
                        Ident("npid", IdentGruppe.NPID, false),
                    ),
                falskIdent = false,
                doedsdato = null,
                doed = false,
            )

        val person = client.getPerson("123")

        person?.navn?.fornavn shouldEqual "Fornavn"
        person?.foedselsdato shouldEqual LocalDate.of(1991, 1, 1)
        person?.identer?.size shouldEqual 4
    }
}
