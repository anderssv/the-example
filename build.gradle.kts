import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.text.RegexOption.IGNORE_CASE

plugins {
    kotlin("jvm") version "1.9.20"
    id("com.github.ben-manes.versions") version "0.49.0"
}

group = "no.mikill"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation(kotlin("test"))
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
    }
}


