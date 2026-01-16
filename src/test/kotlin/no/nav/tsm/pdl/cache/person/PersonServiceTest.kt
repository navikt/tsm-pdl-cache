package no.nav.tsm.pdl.cache.person

import no.nav.tsm.pdl.cache.pdl.IDENT_GRUPPE
import no.nav.tsm.pdl.cache.pdl.Navn
import no.nav.tsm.pdl.cache.pdl.Person
import no.nav.tsm.pdl.cache.pdl.PersonDbResult
import no.nav.tsm.pdl.cache.pdl.PersonRepository
import no.nav.tsm.pdl.cache.person.exceptions.PersonNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito

class PersonServiceTest {
    val repo = Mockito.mock(PersonRepository::class.java)
    val personService = PersonService(repo)


    @Test
    fun testTestpersonShouldThrow() {
        val personResultFnrIdent = PersonDbResult(
            navn = Navn("TEST", null, "TEST"),
            fodselsdato = null,
            aktorId = "aktorid",
            ident = "13116900216",
            historisk = false,
            gruppe = IDENT_GRUPPE.FOLKEREGISTERIDENT,
            falskIdent = false,
            doed = false,
            doedsdato = null
        )
        val personResultAktoerId = personResultFnrIdent.copy(ident = "aktorid", gruppe = IDENT_GRUPPE.AKTORID)
        Mockito.`when`(repo.getPerson("13116900216")).thenReturn(listOf(
            personResultFnrIdent,
            personResultAktoerId
        ))


        assertThrows<PersonNotFoundException>("Test person") { personService.getPerson("13116900216") }
    }
}