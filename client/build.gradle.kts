plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    id("maven-publish")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        withSourcesJar()
    }
}

dependencies {
    api(tsmKtorLibs.core)

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
    testImplementation(ktorLibs.client.mock)
    testImplementation(libs.mockk)
    testImplementation(libs.kotest.assertions)
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
            groupId = "no.nav.tsm"
            artifactId = "pdl-client"
            version = version
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/navikt/tsm-pdl-cache")
            credentials {
                username = "x-access-token"
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
