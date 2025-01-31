package no.nav.tsm.pdl.cache.person

import no.nav.tsm.pdl.cache.person.exceptions.IdentIsMissingException
import no.nav.tsm.pdl.cache.person.exceptions.PersonNotFoundException
import no.nav.tsm.pdl.cache.person.exceptions.ToManyPersonException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class RestControllerAdvice : ResponseEntityExceptionHandler() {
    @ExceptionHandler(IdentIsMissingException::class)
    fun handleIdentIsMissingException(e: IdentIsMissingException): ResponseEntity<String> {
        return ResponseEntity.badRequest().body(e.message)
    }
    @ExceptionHandler(PersonNotFoundException::class)
    fun handlePersonNotFoundException(e: PersonNotFoundException): ResponseEntity<String> {
        return ResponseEntity.notFound().build()
    }

    @ExceptionHandler(ToManyPersonException::class)
    fun handleToManyPersonException(e: ToManyPersonException): ResponseEntity<String> {
        return ResponseEntity(e.message, HttpStatus.CONFLICT)
    }

}
