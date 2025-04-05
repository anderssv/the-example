import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import kotlin.text.RegexOption.IGNORE_CASE

plugins {
    kotlin("jvm") version "2.0.20"
    id("com.github.ben-manes.versions") version "0.49.0"
}

group = "no.mikill"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}



dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("io.ktor:ktor-client-core:2.3.9")
    implementation("io.ktor:ktor-client-cio:2.3.9")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.9")
    implementation("io.ktor:ktor-serialization-jackson:2.3.9")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-client-mock:2.3.9")
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
