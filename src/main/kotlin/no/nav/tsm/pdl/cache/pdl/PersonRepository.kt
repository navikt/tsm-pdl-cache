package no.nav.tsm.pdl.cache.pdl

import java.time.LocalDate
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

data class PersonDbResult(
    val navn: Navn?,
    val fodselsdato: LocalDate?,
    val aktorId: String,
    val ident: String,
    val historisk: Boolean,
    val gruppe: IDENT_GRUPPE,
    val falskIdent: Boolean,
    val doed: Boolean,
    val doedsdato: LocalDate?,
)

class PersonRepository {

    fun deletePersons(aktorIds: List<String>): Int = transaction {
        PersonTable.deleteWhere { PersonTable.aktorId inList aktorIds }
    }

    fun getAktorIds(idents: List<String>): List<String> = transaction {
        IdentTable.select(IdentTable.person)
            .where { IdentTable.ident inList idents }
            .map { it[IdentTable.person] }
            .distinct()
    }

    fun getPerson(ident: String): List<PersonDbResult> = transaction {
        val aktorId =
            IdentTable.select(IdentTable.person)
                .where { IdentTable.ident eq ident }
                .map { it[IdentTable.person] }
                .singleOrNull() ?: return@transaction emptyList()

        PersonTable.innerJoin(IdentTable)
            .selectAll()
            .where { PersonTable.aktorId eq aktorId }
            .map {
                PersonDbResult(
                    navn = it[PersonTable.navn],
                    fodselsdato = it[PersonTable.foedselsdato],
                    aktorId = it[PersonTable.aktorId],
                    ident = it[IdentTable.ident],
                    historisk = it[IdentTable.historisk],
                    gruppe = IDENT_GRUPPE.valueOf(it[IdentTable.gruppe]),
                    falskIdent = it[PersonTable.falskIdentitet],
                    doed = it[PersonTable.doed],
                    doedsdato = it[PersonTable.doedsdato],
                )
            }
    }

    fun getPersons(idents: List<String>): List<PersonDbResult> = transaction {
        PersonTable.innerJoin(IdentTable)
            .selectAll()
            .where { IdentTable.ident inList idents }
            .map {
                PersonDbResult(
                    navn = it[PersonTable.navn],
                    fodselsdato = it[PersonTable.foedselsdato],
                    aktorId = it[PersonTable.aktorId],
                    ident = it[IdentTable.ident],
                    historisk = it[IdentTable.historisk],
                    gruppe = IDENT_GRUPPE.valueOf(it[IdentTable.gruppe]),
                    falskIdent = it[PersonTable.falskIdentitet],
                    doed = it[PersonTable.doed],
                    doedsdato = it[PersonTable.doedsdato],
                )
            }
    }

    fun insertPerson(aktorId: String, person: Person) = transaction {
        PersonTable.insert {
            it[PersonTable.aktorId] = aktorId
            it[PersonTable.foedselsdato] = person.foedselsdato
            it[PersonTable.navn] = person.navn
            it[PersonTable.falskIdentitet] = person.falskIdent
            it[PersonTable.doed] = person.doed
            it[PersonTable.doedsdato] = person.doedsdato
        }

        IdentTable.batchInsert(data = person.identer, shouldReturnGeneratedValues = false) { ident ->
            this[IdentTable.ident] = ident.ident
            this[IdentTable.gruppe] = ident.gruppe.name
            this[IdentTable.historisk] = ident.historisk
            this[IdentTable.person] = aktorId
        }
    }
}
