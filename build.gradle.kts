plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    application
}

group = "com.github.botacz"
version = "1.0-SNAPSHOT"

val ktorVersion = "2.3.8"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")

    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("io.ktor:ktor-server-status-pages:2.3.8")

    testImplementation(kotlin("test"))

    val exposedVersion = "0.50.0"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.postgresql:postgresql:42.7.2")
    testImplementation("com.h2database:h2:2.2.224")
    implementation("io.ktor:ktor-server-websockets:2.3.8")
    implementation("io.ktor:ktor-server-auth:2.3.8")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.8")
    implementation("io.ktor:ktor-server-rate-limit:2.3.8")
}

application {
    mainClass.set("MainKt")
}

kotlin {
    jvmToolchain(21)
}