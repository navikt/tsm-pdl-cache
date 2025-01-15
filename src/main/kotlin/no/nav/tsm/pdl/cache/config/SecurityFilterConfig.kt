package no.nav.tsm.pdl.cache.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.authorizeExchange { exchanges ->
            exchanges
                // Permit all paths under /internal/
                .pathMatchers("/internal/**").permitAll()
                // Everything else requires authentication
                .anyExchange().authenticated()
        }
        .build()
    }
}
