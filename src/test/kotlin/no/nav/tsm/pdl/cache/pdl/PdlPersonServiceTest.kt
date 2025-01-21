package no.nav.tsm.pdl.cache.pdl

import no.nav.tsm.pdl.cache.TestcontainersConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.junit.jupiter.api.Test

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class PdlPersonServiceTest {
    @Test
    fun testSthi() {
    }
}

