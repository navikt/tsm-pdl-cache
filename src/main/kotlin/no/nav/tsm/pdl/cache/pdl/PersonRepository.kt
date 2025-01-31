package no.nav.tsm.pdl.cache.pdl

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tsm.pdl.cache.util.objectMapper
import org.postgresql.util.PGobject
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate

class PersnDbResult(
    val navn: Navn?,
    val fodselsdato: LocalDate?,
    val aktorId: String,
    val ident: String,
    val historisk: Boolean,
    val gruppe: IDENT_GRUPPE
)

@Repository
class PersonRepository(val sqlTemplate: NamedParameterJdbcTemplate) {

    fun deletePersons(aktorIds: List<String>): Int {
        val sql = """DELETE FROM person WHERE aktor_id in (:aktorIds)"""
        return sqlTemplate.update(sql, mapOf("aktorIds" to aktorIds))
    }

    fun getAktorIds(idents: List<String>): List<String> {
        val sql = """
            select distinct aktor_id from identer where ident in (:idents)
        """.trimIndent()
        return sqlTemplate.query(sql, mapOf("idents" to idents)) {
            rs, _ -> rs.getString("aktor_id")
        }
    }

    fun getPersons(idents: List<String>): List<PersnDbResult> {
        val sql = """
            SELECT 
            id.ident as ident,
            id.gruppe as gruppe,
            id.historisk as historisk,
            id.aktor_id as aktor_id,
            p.navn as navn,
            p.aktor_id as p_aktor_id,
            p.fodselsdato as fodselsdato
            FROM identer id inner join person p on id.aktor_id = p.aktor_id WHERE id.ident in (:idents) 
        """
        val persons = sqlTemplate.query(sql, mapOf("idents" to idents)) { rs, _ ->
            PersnDbResult(
                navn = rs.getString("navn")?.let { objectMapper.readValue<Navn>(it.toString()) },
                fodselsdato = rs.getDate("fodselsdato")?.let { it.toLocalDate() },
                aktorId = rs.getString("p_aktor_id"),
                ident = rs.getString("ident"),
                historisk = rs.getBoolean("historisk"),
                gruppe = rs.getString("gruppe").let { IDENT_GRUPPE.valueOf(it) })
        }
        return persons
    }

    fun insertPerson(aktorId: String, person: Person) {
        val personInsert = "INSERT into person(aktor_id, navn, fodselsdato) VALUES(:aktorId, :navn, :fodselsdato)"
        sqlTemplate.update(personInsert, mapOf(
            "aktorId" to aktorId,
            "navn" to person.navn?.let { PGobject().apply {
                type = "jsonb"
                value = objectMapper.writeValueAsString(person.navn)
            } },
            "fodselsdato" to person.foedselsdato
        ))
        val identInserts = "INSERT INTO identer(ident, aktor_id, gruppe, historisk) VALUES(:ident, :aktorId, :gruppe, :historisk)"
        sqlTemplate.batchUpdate(identInserts, person.identer.map {
            mapOf(
                "ident" to it.ident,
                "aktorId" to aktorId,
                "gruppe" to it.gruppe.name,
                "historisk" to it.historisk
            )
        }.toTypedArray())

    }
}
