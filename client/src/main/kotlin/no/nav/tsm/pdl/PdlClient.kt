package no.nav.tsm.pdl

sealed interface PdlClient {
    class UnknownError(cause: Throwable) : Exception(cause)

    suspend fun getPerson(ident: String): Person?

    suspend fun getAktorId(ident: String): String?
}
