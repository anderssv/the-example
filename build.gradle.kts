import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import kotlin.text.RegexOption.IGNORE_CASE

plugins {
    kotlin("jvm") version "2.1.20"
    id("com.github.ben-manes.versions") version "0.52.0"
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
    implementation("io.ktor:ktor-client-cio:3.1.2")
    implementation("io.ktor:ktor-client-content-negotiation:3.1.2")
    implementation("io.ktor:ktor-serialization-jackson:3.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-client-mock:3.1.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
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
