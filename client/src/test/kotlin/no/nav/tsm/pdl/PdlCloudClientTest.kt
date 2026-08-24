package no.nav.tsm.pdl

import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.utils.io.*
import io.mockk.coEvery
import io.mockk.mockk
import java.time.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import no.nav.tsm.ktor.auth.texas.Texas
import no.nav.tsm.ktor.auth.texas.TexasToken
import no.nav.tsm.ktor.core.SimpleNavn
import tools.jackson.module.kotlin.jacksonObjectMapper

class PdlCloudClientTest {
    val texasMock = mockk<Texas>()

    @BeforeTest
    fun setup() {
        coEvery { texasMock.entraIdToken(any(), any()) } returns TexasToken("test-token")
    }

    @Test
    fun `should properly deserialize response`() = testApplication {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/person", request.url.fullPath)
            request.headers["Authorization"] shouldBe "Bearer test-token"

            respond(
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                content = ByteReadChannel(goodResponseBodyJson),
            )
        }

        val pdlClient =
            PdlCloudClient(
                httpClient = HttpClient(mockEngine) {},
                texasClient = texasMock,
            )

        val response = pdlClient.getPerson("hello")

        response?.foedselsdato shouldBe LocalDate.now().minusYears(35)
    }

    @Test
    fun `should be able to get aktorId`() = testApplication {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/person", request.url.fullPath)
            request.headers["Authorization"] shouldBe "Bearer test-token"

            respond(
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                content = ByteReadChannel(goodResponseBodyJson),
            )
        }

        val pdlClient =
            PdlCloudClient(
                httpClient = HttpClient(mockEngine) {},
                texasClient = texasMock,
            )

        val response = pdlClient.getAktorId("hello")

        response shouldBe "test-aktor-id"
    }

    @Test
    fun `404 should result in not found`() = testApplication {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/person", request.url.fullPath)
            request.headers["Authorization"] shouldBe "Bearer test-token"

            respondError(HttpStatusCode.NotFound)
        }

        val pdlClient =
            PdlCloudClient(
                httpClient = HttpClient(mockEngine) {},
                texasClient = texasMock,
            )

        val response = pdlClient.getPerson("hello")
        response shouldBe null
    }
}

private val testJsonObjectMapper = jacksonObjectMapper()

private val goodResponseBodyJson =
    testJsonObjectMapper.writeValueAsString(
        Person(
            navn =
                SimpleNavn(
                    fornavn = "Fornavn",
                    mellomnavn = "Mellomnavn",
                    etternavn = "Etternavn",
                ),
            foedselsdato = LocalDate.now().minusYears(35),
            identer =
                listOf(
                    Ident(
                        ident = "12345678910",
                        gruppe = IdentGruppe.FOLKEREGISTERIDENT,
                        historisk = false,
                    ),
                    Ident(
                        ident = "test-aktor-id",
                        gruppe = IdentGruppe.AKTORID,
                        historisk = false,
                    ),
                ),
            falskIdent = true,
            doed = true,
            doedsdato = null,
        )
    )
