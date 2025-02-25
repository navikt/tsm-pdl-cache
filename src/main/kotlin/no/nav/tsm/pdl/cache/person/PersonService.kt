package no.nav.tsm.pdl.cache.person

import no.nav.tsm.pdl.cache.pdl.IDENT_GRUPPE
import no.nav.tsm.pdl.cache.pdl.Ident
import no.nav.tsm.pdl.cache.pdl.PersnDbResult
import no.nav.tsm.pdl.cache.pdl.Person
import no.nav.tsm.pdl.cache.pdl.PersonRepository
import no.nav.tsm.pdl.cache.person.exceptions.PersonNotFoundException
import no.nav.tsm.pdl.cache.person.exceptions.ToManyPersonException
import org.springframework.stereotype.Service

@Service
class PersonService(private val personRepository: PersonRepository) {

    fun getPerson(ident: String) : Person {
        val persons = mapToPersons(personRepository.getPerson(ident))
        if (persons.size > 1) {
            throw ToManyPersonException("Ident gives to many persons")
        }

        if(persons.isEmpty()) {
            throw PersonNotFoundException("Person not found")
        }

        return persons.single()
    }
}

fun mapToPersons(list: List<PersnDbResult>) : List<Person> {
    return list.groupBy {
        it.aktorId
    }.map {
        val aktorIdIdent = it.value.find { ident -> ident.ident == it.key && ident.gruppe == IDENT_GRUPPE.AKTORID && !ident.historisk }
        if (aktorIdIdent == null) {
            throw IllegalStateException("Fant ikke aktorId i PDL")
        }
        Person(
            navn = aktorIdIdent.navn,
            foedselsdato = aktorIdIdent.fodselsdato,
            identer = it.value.map { ident ->
                Ident(
                    ident = ident.ident,
                    gruppe = ident.gruppe,
                    historisk = ident.historisk
                )
            },
            falskIdent = aktorIdIdent.falskIdent,
            doedsdato = aktorIdIdent.doedsdato,
            doed = aktorIdIdent.doed
        )
    }
}
