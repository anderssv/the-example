import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import kotlin.text.RegexOption.IGNORE_CASE

plugins {
    kotlin("jvm") version "2.1.20"
    id("com.github.ben-manes.versions") version "0.52.0"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    jacoco
}

group = "no.mikill"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.3")
    implementation("io.ktor:ktor-client-core:3.1.2")
    implementation("io.ktor:ktor-client-okhttp:3.1.2")
    implementation("io.ktor:ktor-client-content-negotiation:3.1.2")
    implementation("io.ktor:ktor-serialization-jackson:3.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.postgresql:postgresql:42.7.5")
    implementation("com.zaxxer:HikariCP:6.2.1")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-client-mock:3.1.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    testImplementation("org.slf4j:slf4j-simple:2.0.16") // Eliminates SLF4J warnings in tests
    testImplementation("org.testcontainers:postgresql:1.20.4")
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
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    compileKotlin {
        compilerOptions {
            this.jvmTarget = JvmTarget.JVM_21
        }
    }
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("1.7.1")
    verbose.set(true)
    outputToConsole.set(true)
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}
