package no.nav.tsm.pdl.cache

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import kotlin.test.assertEquals

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class ApplicationTests {

	@Test
	fun contextLoads() {
	}
}
