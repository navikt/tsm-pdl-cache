package no.nav.tsm.pdl

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson3.*
import io.opentelemetry.api.trace.Span
import io.opentelemetry.instrumentation.annotations.WithSpan
import no.nav.tsm.ktor.auth.texas.Texas
import no.nav.tsm.ktor.logger
import no.nav.tsm.ktor.otel.failSpan

class PdlCloudConfig(
    val url: String = "http://tsm-pdl-cache",
    val maxRetries: Int = 5,
)

class PdlCloudClient(
    httpClient: HttpClient,
    private val texasClient: Texas,
    private val pdlCloudConfig: PdlCloudConfig = PdlCloudConfig(),
) : PdlClient {
    private val url = pdlCloudConfig.url
    private val logger = logger()

    private val pdlHttpClient = httpClient.config {
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = pdlCloudConfig.maxRetries)
            exponentialDelay()
        }
        install(ContentNegotiation) {
            jackson {}
        }
    }

    @WithSpan
    override suspend fun getPerson(ident: String): Person? {
        val (token) = getToken()

        val response =
            pdlHttpClient.post("$url/api/person") {
                headers {
                    append("Nav-Consumer-Id", "syk-inn-api")
                    bearerAuth(token)
                }
                contentType(ContentType.Application.Json)
                setBody(PdlQuery(ident))
            }

        return when {
            response.status.isSuccess() ->
                try {
                    response.body<Person>()
                } catch (e: Exception) {
                    failSpan(Span.current(), e)
                    logger.error("Error deserializing PDL response", e)
                    throw PdlClient.UnknownError(e)
                }

            response.status == HttpStatusCode.NotFound -> null
            else -> {
                throw PdlClient.UnknownError(Exception("PDL request failed with status ${response.status}"))
            }
        }
    }

    override suspend fun getAktorId(ident: String): String? {
        val response = getPerson(ident) ?: return null

        return response.identer.firstOrNull { it.gruppe == IdentGruppe.AKTORID && !it.historisk }?.ident
    }

    private suspend fun getToken() = texasClient.entraIdToken("tsm", "tsm-pdl-cache")
}
