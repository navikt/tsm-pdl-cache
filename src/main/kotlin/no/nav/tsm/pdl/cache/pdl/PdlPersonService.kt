package no.nav.tsm.pdl.cache.pdl

import no.nav.tsm.ktor.logger
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class PdlPersonService(val personRepository: PersonRepository) {
    private val logger = logger()

    fun updatePerson(aktorId: String, person: Person?) = transaction {
        if (person == null) {
            personRepository.deletePersons(listOf(aktorId))
            logger.info("received tombstone for aktorId: $aktorId")
            return@transaction
        }

        val aktorIds = personRepository.getAktorIds(person.identer.map { it.ident })
        if (aktorIds.isNotEmpty()) {
            personRepository.deletePersons(aktorIds)
            if (aktorIds.size > 1 || aktorId != aktorIds.first()) {
                logger.info(
                    "Found more than one / different aktorId for person $aktorId, deleted aktorIds: ${aktorIds.joinToString(", ")}"
                )
            }
        }

        personRepository.insertPerson(aktorId, person)
    }
}
