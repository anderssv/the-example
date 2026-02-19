import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import kotlin.text.RegexOption.IGNORE_CASE

plugins {
    kotlin("jvm") version "2.3.10"
    id("com.github.ben-manes.versions") version "0.53.0"
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
    jacoco
}

group = "no.mikill"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.21.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.21.0")
    implementation("io.ktor:ktor-client-core:3.4.0")
    implementation("io.ktor:ktor-client-okhttp:3.4.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.4.0")
    implementation("io.ktor:ktor-serialization-jackson:3.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.postgresql:postgresql:42.7.10")
    implementation("com.zaxxer:HikariCP:7.0.2")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-client-mock:3.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("org.slf4j:slf4j-simple:2.0.17") // Eliminates SLF4J warnings in tests
    testImplementation("org.testcontainers:testcontainers-postgresql:2.0.3")
}

tasks {

    named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates").configure {
        rejectVersionIf {
            candidate.version.contains("-M\\d".toRegex()) ||
                candidate.version.contains("-RC".toRegex(IGNORE_CASE)) ||
                candidate.version.contains("-rc-\\d".toRegex(IGNORE_CASE)) ||
                candidate.version.contains("-alpha[\\.\\d]+".toRegex(IGNORE_CASE)) ||
                candidate.version.contains("-a[\\d]+".toRegex(IGNORE_CASE)) ||
                candidate.version.contains("-preview.\\d".toRegex(IGNORE_CASE)) ||
                candidate.version.contains("-beta", ignoreCase = true)
        }
    }

    test {
        useJUnitPlatform()
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1

        // Enable parallel test execution within JUnit Platform
        systemProperty("junit.jupiter.execution.parallel.enabled", "true")
        systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")

        // Add logging to show parallel execution
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
        }

        // Generate coverage report after tests
        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(false)
            csv.required.set(false)
            html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
        }
    }

    jacoco {
        toolVersion = "0.8.13"
    }

    compileJava {
        sourceCompatibility = "25"
        targetCompatibility = "25"
    }

    compileKotlin {
        compilerOptions {
            this.jvmTarget = JvmTarget.JVM_25
        }
    }
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("1.8.0")
    verbose.set(true)
    outputToConsole.set(true)
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}
