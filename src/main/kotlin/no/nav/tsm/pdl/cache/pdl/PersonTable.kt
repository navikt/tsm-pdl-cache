package no.nav.tsm.pdl.cache.pdl

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tsm.pdl.cache.core.exposedObjectMapper
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.json.jsonb

object PersonTable : Table("person") {
    val aktorId = text("aktor_id")
    val foedselsdato = date("fodselsdato").nullable()
    val navn =
        jsonb(
                "navn",
                { exposedObjectMapper.writeValueAsString(it) },
                { exposedObjectMapper.readValue<Navn>(it) },
            )
            .nullable()
    val falskIdentitet = bool("falsk_identitet")
    val doed = bool("doed")
    val doedsdato = date("doedsdato").nullable()

    override val primaryKey = PrimaryKey(aktorId)
}

object IdentTable : Table("identer") {
    val ident = text("ident")
    val gruppe = text("gruppe")
    val historisk = bool("historisk")
    val person = reference("aktor_id", PersonTable.aktorId, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(ident)
}
