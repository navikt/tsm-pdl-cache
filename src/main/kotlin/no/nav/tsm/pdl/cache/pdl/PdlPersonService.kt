package no.nav.tsm.pdl.cache.pdl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PdlPersonService(val personRepository: PersonRepository) {

    private val logger = LoggerFactory.getLogger(PdlPersonService::class.java)

    fun updatePerson(aktorId: String, person: Person?) {
        if(person == null) {
            return deletePerson(aktorId)
        }
        val personWithIdents = personRepository.hentPerson(person.hentIdenter.map { it.ident })
        if(personWithIdents.isEmpty()) {
            logger.info("Fant ikke person i PDL for aktor $aktorId")
            return personRepository.insertPerson(aktorId, person)
        }

    }

    private fun deletePerson(aktorId: String) {
        logger.info("Deleting person for aktor_id=$aktorId")
        val result = personRepository.deletePerson(aktorId)
        logger.info("Deleted $result person")
        val left = personRepository.hentPerson(listOf(aktorId))
        if (left.isEmpty()) {
            logger.error("Person with aktor_id=$aktorId should not be found")
            throw IllegalStateException("Person with aktor_id=$aktorId should not be found")
        }
    }
}
