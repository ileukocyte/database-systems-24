val ktor_version: String by project
val exposed_version: String by project

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"

    id("io.ktor.plugin") version "2.3.8"
}

group = "io.ileukocyte"
version = "1.0.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(ktor("server-core-jvm", "2.3.8"))
    implementation(ktor("server-netty-jvm", "2.3.8"))
    implementation(kotlinx("serialization-json", "1.6.3"))

    implementation("org.postgresql:postgresql:42.7.1")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")

    implementation("ch.qos.logback:logback-classic:1.4.14")

    testImplementation(kotlin("test"))
}

fun kotlinx(module: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$module:$version"
fun ktor(module: String, version: String) = "io.ktor:ktor-$module:$version"

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