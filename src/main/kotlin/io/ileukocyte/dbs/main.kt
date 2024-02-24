package io.ileukocyte.dbs

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

fun main() {
    embeddedServer(Netty, port = 8000, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()

    configureRouting()
}

fun Application.configureRouting() {
    routing {
        get("/v1/status") {
            val json = buildJsonObject {
                put("version", JsonPrimitive(DatabaseFactory.getDatabaseVersion()))
            }

            call.respondText("$json")
        }
    }
}