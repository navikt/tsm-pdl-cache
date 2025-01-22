package no.nav.tsm.pdl.cache.pdl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PdlPersonService(val personRepository: PersonRepository) {

    private val logger = LoggerFactory.getLogger(PdlPersonService::class.java)

    @Transactional
    fun updatePerson(aktorId: String, person: Person?) {
        if(person == null) {
            personRepository.deletePersons(listOf(aktorId))
            return
        }
        val aktorIds = personRepository.getAktorIds(person.hentIdenter.map { it.ident })
        if(aktorIds.isNotEmpty()) {
            personRepository.deletePersons(aktorIds)
        }
        personRepository.insertPerson(aktorId, person)
    }

    private fun mapToPersons(list: List<PersnDbResult>) : List<Person> {
        return list.groupBy {
            it.aktorId
        }.map {
            val aktorIdIdent = it.value.find { ident -> ident.ident == it.key && ident.gruppe == IDENT_GRUPPE.AKTOR_ID && !ident.historisk }
            if (aktorIdIdent == null) {
                throw IllegalStateException("Fant ikke aktorId i PDL")
            }
            Person(
                navn = aktorIdIdent.navn,
                fodselsdato = aktorIdIdent.fodselsdato,
                hentIdenter = it.value.map { ident ->
                    Ident(
                        ident = ident.ident,
                        gruppe = ident.gruppe,
                        historisk = ident.historisk
                    )
                }
            )
        }

    }
}
