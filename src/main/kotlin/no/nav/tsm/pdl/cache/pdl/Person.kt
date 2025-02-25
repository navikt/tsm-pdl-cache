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
    val foedselsdato: LocalDate?,
    val identer: List<Ident>,
    val falskIdent: Boolean,
    val dodsdato: LocalDate?
)



data class PdlNavn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val metadata: Metadata
)
data class FalskIdentitet(
    val erFalsk: Boolean,
)
data class Doedsfall(
    val doedsdato: LocalDate,
    val metadata: Metadata
)

data class HentPerson (
    val foedselsdato: List<Foedselsdato>,
    val navn: List<PdlNavn>,
    val falskIdentitet: FalskIdentitet?,
    val doedsfall: List<Doedsfall>
)

data class PdlPerson(
    val hentPerson: HentPerson,
    val hentIdenter: HentIdenter
)

data class HentIdenter(
    val identer: List<Ident>
)

data class Foedselsdato(
    val foedselsdato: LocalDate?,
    val metadata: Metadata
)

data class Metadata(val historisk: Boolean)

fun Person.getAktorId() = identer.single { it.gruppe == IDENT_GRUPPE.AKTORID && !it.historisk }.ident
