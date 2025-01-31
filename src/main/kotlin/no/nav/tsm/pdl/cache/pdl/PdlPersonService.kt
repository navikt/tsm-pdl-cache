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
            logger.info("received tombstone for aktorId: $aktorId")
            return
        }
        val aktorIds = personRepository.getAktorIds(person.identer.map { it.ident })
        if(aktorIds.isNotEmpty()) {
            personRepository.deletePersons(aktorIds)
            if(aktorIds.size > 1 || aktorId != aktorIds.first()) {
                logger.info("Found more than one / different aktorId for person $aktorId, deleted aktorIds: ${aktorIds.joinToString(", ")}")
            }
        }
        personRepository.insertPerson(aktorId, person)
    }
}
