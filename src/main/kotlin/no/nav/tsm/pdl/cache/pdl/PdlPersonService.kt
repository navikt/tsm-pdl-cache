package no.nav.tsm.pdl.cache.pdl

import no.nav.tsm.ktor.logger
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class PdlPersonService(val personRepository: PersonRepository) {
    private val logger = logger()

    fun updatePerson(aktorId: String, person: Person) = transaction {
        val aktorIds = personRepository.getAktorIds(person.identer.map { it.ident })
        if (aktorIds.isNotEmpty()) {
            logger.debug("Found ${aktorIds.size} aktorIds for person $aktorId, deleting them")
            personRepository.deletePersons(aktorIds)
            if (aktorIds.size > 1 || aktorId != aktorIds.first()) {
                logger.info(
                    "Found more than one / different aktorId for person $aktorId, deleted aktorIds: ${aktorIds.joinToString(", ")}"
                )
            }
        }

        logger.debug("Inserting person for aktorId: $aktorId, has ${person.identer.size} idents")
        personRepository.insertPerson(aktorId, person)
    }

    fun tombstonePerson(aktorId: String) = transaction {
        personRepository.deletePersons(listOf(aktorId))
        logger.info("received tombstone for aktorId: $aktorId")
    }
}
