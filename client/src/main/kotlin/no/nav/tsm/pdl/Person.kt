package no.nav.tsm.pdl

import java.time.LocalDate
import no.nav.tsm.ktor.core.SimpleNavn

enum class IdentGruppe {
    AKTORID,
    FOLKEREGISTERIDENT,
    NPID,
}

data class Ident(
    val ident: String,
    val gruppe: IdentGruppe,
    val historisk: Boolean,
)

data class Person(
    val navn: SimpleNavn?,
    val foedselsdato: LocalDate?,
    val identer: List<Ident>,
    val falskIdent: Boolean,
    val doed: Boolean,
    val doedsdato: LocalDate?,
)

fun Person.getAktorId() = identer.single { it.gruppe == IdentGruppe.AKTORID && !it.historisk }.ident
