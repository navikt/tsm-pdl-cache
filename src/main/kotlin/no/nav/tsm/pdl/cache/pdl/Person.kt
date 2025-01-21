package no.nav.tsm.pdl.cache.pdl

import java.time.LocalDate

enum class IDENT_GRUPPE {
    AKTOR_ID,
    FOLKEREGISTERIDENT,
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

class Person(
    val navn: Navn,
    val fodselsdato: LocalDate,
    val hentIdenter: List<Ident>
)
