package no.nav.tsm.pdl.plugin

import io.kotest.matchers.equals.shouldEqual
import io.ktor.server.application.install
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.testing.testApplication
import no.nav.tsm.pdl.PdlClient
import org.junit.Test

class PdlPluginTest {

    @Test
    fun `plugin should install and make pdlclient available`() = testApplication {
        application.install(PdlPlugin)

        startApplication()

        val pdlClient: PdlClient by application.dependencies
        val result = pdlClient.getPerson("12345678901")

        // Should be mocked response
        result?.navn?.displayName() shouldEqual "Test Testesen Testson"
    }
}
