val ktorVersion: String by project
val exposedVersion: String by project

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"

    id("io.ktor.plugin") version "2.3.9"
}

group = "io.ileukocyte"
version = "2.0.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(ktor("server-core-jvm"))
    implementation(ktor("server-netty-jvm"))
    implementation(kotlinx("serialization-json", "1.6.3"))

    implementation("org.postgresql:postgresql:42.7.2")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    implementation("ch.qos.logback:logback-classic:1.5.3")

    testImplementation(kotlin("test"))
}

fun ktor(module: String, version: String = ktorVersion) = "io.ktor:ktor-$module:$version"
fun kotlinx(module: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$module:$version"

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest.attributes["Main-Class"] = "io.ileukocyte.dbs.MainKt"

    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree)

    from(dependencies)

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}