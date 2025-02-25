package no.nav.tsm.pdl.cache.pdl

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tsm.pdl.cache.util.objectMapper
import org.postgresql.util.PGobject
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate

class PersnDbResult(
    val navn: Navn?,
    val fodselsdato: LocalDate?,
    val aktorId: String,
    val ident: String,
    val historisk: Boolean,
    val gruppe: IDENT_GRUPPE,
    val falskIdent: Boolean,
    val doed: Boolean,
    val doedsdato: LocalDate?
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

    fun getPerson(ident: String) : List<PersnDbResult> {
        val sql = """
            SELECT
                id.ident as ident,
                id.gruppe as gruppe,
                id.historisk as historisk,
                id.aktor_id as aktor_id,
                p.navn as navn,
                p.aktor_id as p_aktor_id,
                p.fodselsdato as fodselsdato,
                p.falsk_identitet as falsk_ident,
                p.doed as doed,
                p.doedsdato as doedsdato
            FROM identer id inner join person p on id.aktor_id = p.aktor_id WHERE id.aktor_id = (select t.aktor_id from identer t where t.ident = :ident);
        """
        val persons = sqlTemplate.query(sql, mapOf("ident" to ident)) { rs, _ ->
            PersnDbResult(
                navn = rs.getString("navn")?.let { objectMapper.readValue<Navn>(it) },
                fodselsdato = rs.getDate("fodselsdato")?.let { it.toLocalDate() },
                aktorId = rs.getString("p_aktor_id"),
                ident = rs.getString("ident"),
                historisk = rs.getBoolean("historisk"),
                gruppe = rs.getString("gruppe").let { IDENT_GRUPPE.valueOf(it) },
                falskIdent = rs.getBoolean("falsk_ident"),
                doed = rs.getBoolean("doed"),
                doedsdato = rs.getDate("doedsdato")?.toLocalDate()
            )
        }
        return persons
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
            p.fodselsdato as fodselsdato,
            p.falsk_identitet as falsk_ident,
            p.doed as doed,
            p.doedsdato as doedsdato
            FROM identer id inner join person p on id.aktor_id = p.aktor_id WHERE id.ident in (:idents) 
        """
        val persons = sqlTemplate.query(sql, mapOf("idents" to idents)) { rs, _ ->
            PersnDbResult(
                navn = rs.getString("navn")?.let { objectMapper.readValue<Navn>(it) },
                fodselsdato = rs.getDate("fodselsdato")?.let { it.toLocalDate() },
                aktorId = rs.getString("p_aktor_id"),
                ident = rs.getString("ident"),
                historisk = rs.getBoolean("historisk"),
                gruppe = rs.getString("gruppe").let { IDENT_GRUPPE.valueOf(it) },
                falskIdent = rs.getBoolean("falsk_ident"),
                doed = rs.getBoolean("doed"),
                doedsdato = rs.getDate("doedsdato")?.toLocalDate()
            )
        }
        return persons
    }

    fun insertPerson(aktorId: String, person: Person) {
        val personInsert = "INSERT into person(aktor_id, navn, fodselsdato, falsk_identitet, doed, doedsdato) VALUES(:aktorId, :navn, :fodselsdato, :falskIdent, :doed, :doedsdato)"
        sqlTemplate.update(personInsert, mapOf(
            "aktorId" to aktorId,
            "navn" to person.navn?.let { PGobject().apply {
                type = "jsonb"
                value = objectMapper.writeValueAsString(person.navn)
            } },
            "fodselsdato" to person.foedselsdato,
            "falskIdent" to person.falskIdent,
            "doed" to person.doed,
            "doedsdato" to person.doedsdato
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
