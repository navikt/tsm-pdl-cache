package no.nav.tsm.pdl.cache.pdl

import java.time.LocalDate

enum class IDENT_GRUPPE {
    AKTORID,
    FOLKEREGISTERIDENT,
    NPID,
}

data class Ident(
    val ident: String,
    val gruppe: IDENT_GRUPPE,
    val historisk: Boolean,
)
data class Navn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
)

data class Person(
    val navn: Navn?,
    val foedselsdato: LocalDate,
    val identer: List<Ident>
)



data class PdlNavn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val historisk: Boolean,
)

data class HentPerson (
    val foedsel: List<Foedsel>,
    val navn: List<PdlNavn>,
)
data class PdlPerson(
    val hentPerson: HentPerson,
    val hentIdenter: HentIdenter
)

data class HentIdenter(
    val identer: List<Ident>
)

data class Foedsel(
    val foedselsdato: LocalDate,
    val historisk: Boolean
)

fun Person.getAktorId() = identer.single { it.gruppe == IDENT_GRUPPE.AKTORID && !it.historisk }.ident
