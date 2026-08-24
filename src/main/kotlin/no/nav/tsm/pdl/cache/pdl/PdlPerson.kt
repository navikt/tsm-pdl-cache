package no.nav.tsm.pdl.cache.pdl

import java.time.LocalDate
import no.nav.tsm.pdl.Ident
import no.nav.tsm.pdl.Metadata

data class PdlNavn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val gyldigFraOgMed: String?,
    val metadata: Metadata,
)

data class FalskIdentitet(val erFalsk: Boolean)

data class Doedsfall(
    val doedsdato: LocalDate?,
    val metadata: Metadata,
)

data class HentPerson(
    val foedselsdato: List<Foedselsdato>?,
    val foedsel: List<Foedselsdato>?,
    val navn: List<PdlNavn>,
    val falskIdentitet: FalskIdentitet?,
    val doedsfall: List<Doedsfall>,
)

data class PdlPerson(
    val hentPerson: HentPerson,
    val hentIdenter: HentIdenter,
)

data class HentIdenter(val identer: List<Ident>)

data class Foedselsdato(
    val foedselsdato: LocalDate?,
    val metadata: Metadata,
)
