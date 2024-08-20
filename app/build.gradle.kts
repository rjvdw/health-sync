val kotlinx_coroutines_reactor_version: String by project
val kotlinx_serialization_json_version: String by project
val ktor_version: String by project
val logback_version: String by project
val r2dbc_postgresql_version: String by project
val reactor_kotlin_extensions_version: String by project

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    kotlin("plugin.serialization") version "2.0.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:r2dbc-postgresql:$r2dbc_postgresql_version")

    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:$reactor_kotlin_extensions_version")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinx_coroutines_reactor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinx_serialization_json_version")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    implementation("ch.qos.logback:logback-classic:$logback_version")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "dev.rdcl.health.SyncKt"
}
