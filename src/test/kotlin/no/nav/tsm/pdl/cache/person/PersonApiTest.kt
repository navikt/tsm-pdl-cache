package no.nav.tsm.pdl.cache.person

import no.nav.tsm.pdl.cache.config.SecurityConfig
import no.nav.tsm.pdl.cache.config.TestJwtDecoder
import no.nav.tsm.pdl.cache.pdl.IDENT_GRUPPE
import no.nav.tsm.pdl.cache.pdl.Ident
import no.nav.tsm.pdl.cache.pdl.Navn
import no.nav.tsm.pdl.cache.pdl.Person
import no.nav.tsm.pdl.cache.person.exceptions.PersonNotFoundException
import no.nav.tsm.pdl.cache.person.exceptions.ToManyPersonException
import no.nav.tsm.pdl.cache.util.JwtUtil
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDate
import kotlin.test.assertEquals

@WebFluxTest(PersonApi::class)
@Import(SecurityConfig::class, TestJwtDecoder::class)
@AutoConfigureWebFlux
class PersonApiTest {

    @MockitoBean
    private lateinit var personService: PersonService

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun getUnauthorized() {
        webTestClient
            .get()
            .uri("/api/person")
            .exchange().expectStatus().isUnauthorized
    }

    @Test
    fun getUnauthorizedWithIncorrectAudience() {
        webTestClient
            .get()
            .uri("/api/person")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${JwtUtil.createJwt(audience = "other")}")
            .exchange().expectStatus().isUnauthorized
    }

    @Test
    fun testGetPersonWithoutIdent() {
        webTestClient
            .get()
            .uri("/api/person")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${JwtUtil.createJwt()}")
            .exchange().expectStatus().isBadRequest

    }

    @Test
    fun testGet404() {
        Mockito.`when`(personService.getPerson("123")).thenAnswer {  throw  PersonNotFoundException("Person not found"); }
        val result =
            webTestClient.get().uri("/api/person")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${JwtUtil.createJwt()}")
                .header("ident", "123").exchange().returnResult(Person::class.java)
        assertEquals(HttpStatus.NOT_FOUND.value(), result.status.value())
    }

    @Test
    fun testToManyPersons() {
        Mockito.`when`(personService.getPerson("123")).thenAnswer {  throw  ToManyPersonException("Person not found"); }
        val result =
            webTestClient.get().uri("/api/person")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${JwtUtil.createJwt()}")
                .header("ident", "123").exchange().returnResult(Person::class.java)
        assertEquals(HttpStatus.CONFLICT.value(), result.status.value())
    }

    @Test
    fun testGetPerson() {
        Mockito.`when`(personService.getPerson("123")).thenReturn(Person(
            navn = Navn("Fornavn", "Mellomnavn", "Etternavn"),
            foedselsdato = LocalDate.of(1991, 1, 1),
            identer = listOf(
                Ident("aktorId", IDENT_GRUPPE.AKTORID, false),
                Ident("123", IDENT_GRUPPE.FOLKEREGISTERIDENT, false),
                Ident("321", IDENT_GRUPPE.FOLKEREGISTERIDENT, true),
                Ident("npid", IDENT_GRUPPE.NPID, false)
            ),
            falskIdent = false,
            doedsdato = null,
            doed = false,
        ))
        val result =
            webTestClient.get().uri("/api/person")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${JwtUtil.createJwt()}")
                .header("ident", "123").exchange().returnResult(Person::class.java)
        assertEquals(HttpStatus.OK.value(), result.status.value())
    }
}
