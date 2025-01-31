package no.nav.tsm.pdl.cache.config

import io.jsonwebtoken.security.Keys
import no.nav.tsm.pdl.cache.util.JwtUtil.secretKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtClaimNames.AUD
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder

@TestConfiguration
class TestJwtDecoder {

    @Bean
    fun jwtDecoder(@Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}") issuer: String,
                   @Value("\${spring.security.oauth2.resourceserver.jwt.audiences}") audience: String): ReactiveJwtDecoder {
        val key = Keys.hmacShaKeyFor(secretKey.toByteArray())
        val decoder = NimbusReactiveJwtDecoder.withSecretKey(key).build()
        val issuer = JwtValidators.createDefaultWithIssuer(issuer)
        val audience = JwtClaimValidator<List<String>>(AUD) { it.contains(audience) }
        val validators = DelegatingOAuth2TokenValidator(issuer, audience)
        decoder.setJwtValidator(validators)
        return decoder
    }
}
