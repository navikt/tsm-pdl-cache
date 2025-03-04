package no.nav.tsm.pdl.cache.pdl

import kotlinx.coroutines.runBlocking
import no.nav.tsm.pdl.cache.TestcontainersConfiguration
import no.nav.tsm.pdl.cache.person.PersonService
import no.nav.tsm.pdl.cache.person.mapToPersons
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals


@Import(TestcontainersConfiguration::class)
@SpringBootTest
class PdlPersonServiceTest() {

    @Autowired
    private lateinit var pdlPersonService: PdlPersonService

    @Autowired
    private lateinit var personService: PersonService

    @Autowired private lateinit var personRepository: PersonRepository

    @Test
    fun savePersonWithoutNameAndFoedsel() {
        val person = person().copy(navn = null, foedselsdato = null)
        pdlPersonService.updatePerson(person.getAktorId(), person)
        val personFromDb = mapToPersons(personRepository.getPersons(listOf(person.getAktorId()))).first()
        assertEquals(person, personFromDb)
    }

    @Test
    fun saveNewPerson() {
        val aktorId = UUID.randomUUID().toString()
        val person = Person(
            Navn(
                fornavn = "Fornavn",
                mellomnavn = "Mellomnavn",
                etternavn = "Etternavn",
            ), LocalDate.of(1991, 1, 1),
            identer = listOf(
                Ident(
                    ident = aktorId,
                    IDENT_GRUPPE.AKTORID,
                    historisk = false
                )
            ),
            falskIdent = false,
            doedsdato = null,
            doed = false,
        )
        pdlPersonService.updatePerson(aktorId, person)
    }

    @Test
    fun `Insert person with two aktor id (one historisk)`() {
        val person = person(
            listOf(Ident(
                ident = UUID.randomUUID().toString(),
                IDENT_GRUPPE.AKTORID,
                historisk = true
            ))
        )

        pdlPersonService.updatePerson(person.getAktorId(), person)
        runBlocking {
            val person = personService.getPerson(person.getAktorId())
            assertThat(person.identer).hasSize(2)
        }
    }

    @Test
    fun `Insert person where historisk aktorId is another person`() {
        val firstPerson = person()
        pdlPersonService.updatePerson(firstPerson.getAktorId(), firstPerson)
        val lastPerson = person(listOf(
            Ident(
                ident = firstPerson.getAktorId(),
                IDENT_GRUPPE.AKTORID,
                historisk = true
            )
        ))
        pdlPersonService.updatePerson(lastPerson.getAktorId(), lastPerson)
        assertEquals(listOf(lastPerson.getAktorId()), personRepository.getAktorIds(listOf(firstPerson.getAktorId())))
    }

    @Test
    fun `Test merge person 1 and 2`() {
        val (person1, person2, person3) = setUpTest()

        var person4 = person1.copy(
            identer = listOf(Ident("FNR1", IDENT_GRUPPE.FOLKEREGISTERIDENT, true),
                Ident("FNR2", IDENT_GRUPPE.FOLKEREGISTERIDENT, true),
                Ident("FNR3", IDENT_GRUPPE.FOLKEREGISTERIDENT, false),
                Ident(person1.getAktorId(), IDENT_GRUPPE.AKTORID, true),
                Ident(person2.getAktorId(), IDENT_GRUPPE.AKTORID, false))
        )

        pdlPersonService.updatePerson(person4.getAktorId(), person4)

        assertEquals(listOf(person4.getAktorId()).sorted(), personRepository.getAktorIds(listOf("FNR1", "FNR2", "FNR3", person1.getAktorId(), person2.getAktorId())).sorted())
        assertEquals(listOf(person3.getAktorId()), personRepository.getAktorIds(listOf("FNR4", person3.getAktorId())))

    }

    @Test
    fun `Test split person`() {
        val (person1, person2, person3) = setUpTest()

        val person4 = person(
            listOf(
                Ident("FNR1", IDENT_GRUPPE.FOLKEREGISTERIDENT, true),
            )
        )

        val person5 = person(listOf(
            Ident("FNR2", IDENT_GRUPPE.FOLKEREGISTERIDENT, true),
        ))


        pdlPersonService.updatePerson(person4.getAktorId(), person4)
        pdlPersonService.updatePerson(person5.getAktorId(), person5)

        assertEquals(listOf(person4.getAktorId()), personRepository.getAktorIds(listOf("FNR1")).sorted())
        assertEquals(listOf(person4.getAktorId()), personRepository.getAktorIds(listOf(person4.getAktorId())).sorted())

        assertEquals(listOf(person5.getAktorId()), personRepository.getAktorIds(listOf("FNR2")).sorted())
        assertEquals(listOf(person5.getAktorId()), personRepository.getAktorIds(listOf(person5.getAktorId())).sorted())

        assertEquals(listOf(person2.getAktorId()), personRepository.getAktorIds(listOf("FNR3")).sorted())
        assertEquals(listOf(person2.getAktorId()), personRepository.getAktorIds(listOf(person2.getAktorId())).sorted())

        assertEquals(listOf(person3.getAktorId()), personRepository.getAktorIds(listOf("FNR4")).sorted())
        assertEquals(listOf(person3.getAktorId()), personRepository.getAktorIds(listOf(person3.getAktorId())).sorted())

        assertEquals(emptyList(), personRepository.getAktorIds(listOf(person1.getAktorId())).sorted())
    }

    private fun setUpTest(): Triple<Person, Person, Person> {
        val person1 = person(
            listOf(
                Ident("FNR1", IDENT_GRUPPE.FOLKEREGISTERIDENT, true),
                Ident("FNR2", IDENT_GRUPPE.FOLKEREGISTERIDENT, false),
            )
        )
        val person2 = person(
            listOf(
                Ident("FNR3", IDENT_GRUPPE.FOLKEREGISTERIDENT, false),
            )
        )
        val person3 = person(
            listOf(
                Ident("FNR4", IDENT_GRUPPE.FOLKEREGISTERIDENT, false),
            )
        )

        pdlPersonService.updatePerson(person1.getAktorId(), person1)
        pdlPersonService.updatePerson(person2.getAktorId(), person2)
        pdlPersonService.updatePerson(person3.getAktorId(), person3)

        assertEquals(listOf(person1.getAktorId()), personRepository.getAktorIds(listOf("FNR1", "FNR2")))
        assertEquals(listOf(person2.getAktorId()), personRepository.getAktorIds(listOf("FNR3")))
        assertEquals(listOf(person3.getAktorId()), personRepository.getAktorIds(listOf("FNR4")))
        assertEquals(
            listOf(person1.getAktorId(), person2.getAktorId(), person3.getAktorId()).sorted(),
            personRepository.getAktorIds(listOf("FNR1", "FNR2", "FNR3", "FNR4")).sorted()
        )
        return Triple(person1, person2, person3)
    }
}

private fun person(identer: List<Ident> = listOf()): Person {
    return Person(
        Navn(
            fornavn = "Fornavn",
            mellomnavn = "Mellomnavn",
            etternavn = "Etternavn",
        ), LocalDate.of(1991, 1, 1),
        identer = listOf(
            Ident(
                ident = UUID.randomUUID().toString(),
                IDENT_GRUPPE.AKTORID,
                historisk = false
            )
        ) + identer,
        falskIdent = false,
        doedsdato = null,
        doed = false,
    )
}
