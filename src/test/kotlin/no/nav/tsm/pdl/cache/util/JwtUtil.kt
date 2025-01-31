package no.nav.tsm.pdl.cache.util

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Date
import java.util.UUID

object JwtUtil {
    val secretKey = UUID.randomUUID().toString()
    fun createJwt(issuer: String = "issuer", audience: String = "audience"): String? {
        return Jwts.builder()
            .audience().add(audience).and()
            .issuer(issuer)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + 3600000)) // 1 hour
            .signWith(Keys.hmacShaKeyFor(secretKey.toByteArray()))
            .compact()
    }
}
