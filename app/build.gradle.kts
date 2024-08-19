plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:r2dbc-postgresql:1.0.5.RELEASE")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "dev.rdcl.health.SyncKt"
}
