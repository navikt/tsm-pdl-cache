package no.nav.tsm.pdl.cache.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tsm.pdl.cache.util.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

class PersnDbResult(
    val navn: Navn,
    val fodselsdato: LocalDate,
    val aktorId: String,
    val ident: String,
    val historisk: Boolean,
    val gruppe: IDENT_GRUPPE
)

@Repository
class PersonRepository(val sqlTemplate: NamedParameterJdbcTemplate) {

    private val logger = LoggerFactory.getLogger(PersonRepository::class.java)
    @Transactional
    fun deletePerson(aktorId: String): Int {
        val sql = "DELETE FROM PERSON WHERE aktor_id = ?"
        return sqlTemplate.update(sql, mapOf("aktor_id" to aktorId))
    }

    fun hentPerson(idents: List<String>): List<PersnDbResult> {
        val sql = "SELECT * FROM identer id inner join person p on id.aktor_id = p.aktor_id WHERE id.ident in (:idents)"
        val persons = sqlTemplate.query(sql, mapOf(":idents" to idents)) { rs, _ ->
            PersnDbResult(
                navn = rs.getString("navn").let { objectMapper.readValue<Navn>(it) },
                fodselsdato = rs.getDate("fodselsdato").toLocalDate(),
                aktorId = rs.getString("p.aktor_id"),
                ident = rs.getString("id.ident"),
                historisk = rs.getBoolean("historisk"),
                gruppe = rs.getString("gruppe").let { IDENT_GRUPPE.valueOf(it) })
        }
        return persons
    }

    @Transactional
    fun insertPerson(aktorId: String, person: Person) {
        val personInsert = "INSERT into person(aktor_id, navn, fodselsdato) VALUES(:aktorId, :navn, :fodselsdato)"
        sqlTemplate.update(personInsert, mapOf(
            "aktorId" to aktorId,
            "navn" to objectMapper.writeValueAsString(person.navn),
            "fodselsdato" to person.fodselsdato
        ))
        val identInserts = "INSERT INTO identer(ident, aktor_id, gruppe, historisk) VALUES(:idents, :aktorId, :gruppe, :historisk)"
        val identInsertsResult = sqlTemplate.batchUpdate(identInserts, person.hentIdenter.map {
            mapOf(
                "ident" to it.ident,
                "aktorId" to aktorId,
                "gruppe" to it.gruppe,
                "historisk" to it.historisk
            )
        }.toTypedArray())

        logger.info("Ferdig med $personInsert person til $identInsertsResult identer")

    }
}
