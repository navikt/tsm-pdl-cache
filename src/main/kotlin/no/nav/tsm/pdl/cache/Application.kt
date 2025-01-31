package no.nav.tsm.pdl.cache

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity

@SpringBootApplication
@EnableReactiveMethodSecurity
class Application

fun main(args: Array<String>) {
	runApplication<Application>(*args)
}
