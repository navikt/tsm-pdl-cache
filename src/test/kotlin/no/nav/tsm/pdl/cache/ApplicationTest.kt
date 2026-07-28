package no.nav.tsm.pdl.cache

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.kotest.matchers.equals.shouldEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.plugins.di.*
import io.ktor.server.testing.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import no.nav.tsm.pdl.cache.pdl.Navn
import no.nav.tsm.pdl.cache.pdl.Person
import no.nav.tsm.pdl.cache.testutils.Environment
import no.nav.tsm.pdl.cache.testutils.WithPostgresAndKafka
import no.nav.tsm.pdl.cache.testutils.createIntegrationEnvironment

class ApplicationTest : WithPostgresAndKafka() {

    @BeforeTest
    fun setup() {
        recreateTopics()
    }

    private suspend fun ApplicationTestBuilder.configureTest() {
        application {
            dependencies {
                provide<Environment>() { createIntegrationEnvironment(postgres, kafka) }
            }

            module()
        }

        startApplication()
    }

    @Test
    fun `test kafka consuming`() = testApplication {
        configureTest()

        val aktorId = "2233445566778"
        val pdlJson = createPdlJson(aktorId = aktorId)
        produce("pdl.pdl-persondokument-v1", aktorId, pdlJson.toByteArray())

        val client = testHttpClient()
        val response = withTimeoutOrNull(10.seconds) { getPerson(client, "17059012345") }
        val body = response?.body<Person>()

        body.shouldNotBeNull()
        body.navn shouldEqual Navn(fornavn = "KARI", mellomnavn = null, etternavn = "NORDMANN")
    }

    @Test
    fun `test kafka tombstone consuming`() = testApplication {
        configureTest()

        val aktorId = "2233445566778"
        val pdlJson = createPdlJson(aktorId = aktorId)
        produce("pdl.pdl-persondokument-v1", aktorId, pdlJson.toByteArray())

        val client = testHttpClient()
        val response = withTimeoutOrNull(10.seconds) { getPerson(client, "17059012345") }
        val body = response?.body<Person>()

        body.shouldNotBeNull()
        body.navn shouldEqual Navn(fornavn = "KARI", mellomnavn = null, etternavn = "NORDMANN")

        produce("pdl.pdl-persondokument-v1", aktorId, null)

        delay(1000.milliseconds)
        val nextResponse =
            client.get("/api/person") {
                contentType(ContentType.Application.Json)
                header("ident", 17059012345)
            }

        nextResponse.status shouldEqual HttpStatusCode.NotFound
    }
}

private suspend fun getPerson(client: HttpClient, ident: String): HttpResponse {
    val response =
        client.get("/api/person") {
            contentType(ContentType.Application.Json)
            header("ident", ident)
        }

    return if (response.status == HttpStatusCode.NotFound) {
        println("Person not found yet, retrying...")
        delay(1000.milliseconds)
        getPerson(client, ident)
    } else {
        response
    }
}

private fun ApplicationTestBuilder.testHttpClient(): HttpClient {
    return createClient {
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
            }
        }
    }
}

private fun createPdlJson(aktorId: String): String =
    """
    {
      "hentPerson": {
        "foedselsdato": [
          {
            "foedselsdato": "1990-05-17",
            "metadata": {
              "historisk": false,
              "master": "FREG"
            }
          }
        ],
        "foedsel": [
          {
            "foedselsdato": "1990-05-17",
            "metadata": {
              "historisk": false,
              "master": "FREG"
            }
          }
        ],
        "navn": [
          {
            "fornavn": "KARI",
            "mellomnavn": null,
            "etternavn": "NORDMANN",
            "gyldigFraOgMed": "1990-05-17",
            "metadata": {
              "historisk": false,
              "master": "FREG"
            }
          }
        ],
        "falskIdentitet": {
          "erFalsk": false
        },
        "doedsfall": []
      },
      "hentIdenter": {
        "identer": [
          {
            "ident": "$aktorId",
            "gruppe": "AKTORID",
            "historisk": false
          },
          {
            "ident": "17059012345",
            "gruppe": "FOLKEREGISTERIDENT",
            "historisk": false
          }
        ]
      }
    }
    """
        .trimIndent()
