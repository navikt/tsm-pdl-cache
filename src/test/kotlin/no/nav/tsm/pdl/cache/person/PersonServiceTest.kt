package no.nav.tsm.pdl.cache.person

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import no.nav.tsm.pdl.cache.pdl.IDENT_GRUPPE
import no.nav.tsm.pdl.cache.pdl.Navn
import no.nav.tsm.pdl.cache.pdl.PersonDbResult
import no.nav.tsm.pdl.cache.pdl.PersonRepository
import no.nav.tsm.pdl.cache.person.exceptions.PersonNotFoundException

class PersonServiceTest {
    val repo = mockk<PersonRepository>()
    val personService = PersonService(personRepository = repo)

    @Test
    fun testTestpersonShouldThrow() {
        val personResultFnrIdent =
            PersonDbResult(
                navn = Navn("TEST", null, "TEST"),
                fodselsdato = null,
                aktorId = "aktorid",
                ident = "13116900216",
                historisk = false,
                gruppe = IDENT_GRUPPE.FOLKEREGISTERIDENT,
                falskIdent = false,
                doed = false,
                doedsdato = null,
            )

        val personResultAktoerId = personResultFnrIdent.copy(ident = "aktorid", gruppe = IDENT_GRUPPE.AKTORID)
        every { repo.getPerson("13116900216") } returns
            listOf(
                personResultFnrIdent,
                personResultAktoerId,
            )

        shouldThrow<PersonNotFoundException> { personService.getPerson("13116900216") }
    }
}
