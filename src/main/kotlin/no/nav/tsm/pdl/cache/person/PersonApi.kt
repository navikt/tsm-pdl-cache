package no.nav.tsm.pdl.cache.person

import no.nav.tsm.pdl.cache.pdl.Person
import no.nav.tsm.pdl.cache.person.exceptions.IdentIsMissingException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class PersonApi(private val personService: PersonService) {

    @GetMapping("/api/person")
    suspend fun getPerson(@RequestHeader("ident") ident: String?): Person {
        if(ident == null) {
            throw IdentIsMissingException("Ident mangler")
        }
        return personService.getPerson(ident)
    }
}
