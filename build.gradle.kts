import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.spotless)
    alias(libs.plugins.gradle.versions)
    alias(libs.plugins.flyway)
}

group = "no.nav.tsm"
version = "1.0.0"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

tasks {
    shadowJar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        mergeServiceFiles {}
        from("src/main/resources/logback.xml") {
            into("/")
        }
    }

    configure<SpotlessExtension> {
        kotlin { ktfmt("0.64").kotlinlangStyle().configure {
            it.setMaxWidth(120)
            it.setContinuationIndent(4)
        } }
        check {
            dependsOn("spotlessApply")
        }
    }
}

dependencies {
    implementation(ktorLibs.serialization.jackson3)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.di)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.metrics.micrometer)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
    implementation(ktorLibs.client.core)
    implementation(ktorLibs.client.apache5)
    implementation(ktorLibs.client.contentNegotiation)

    implementation(libs.kafka.client)
    implementation(libs.logback.classic)
    implementation(libs.logback.encoder)

    implementation(libs.postgresql)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.json)
    implementation(libs.exposed.date)
    implementation(libs.flyway.postgres)
    implementation(libs.flyway.core)

    implementation(libs.tsm.sykmeldinger.input)
    implementation(tsmKtorLibs.core)
    implementation(tsmKtorLibs.auth)

    testImplementation(tsmKtorLibs.kafka.test)
    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
    testImplementation(ktorLibs.client.mock)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.mockk)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.kafka)
}
