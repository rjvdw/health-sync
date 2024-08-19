package dev.rdcl.health

fun getEnv(name: String): String? = System.getenv(name)

fun getEnvRequired(name: String): String =
    System.getenv(name) ?: throw NullPointerException("Missing required environment variable $name")
