package io.ileukocyte.dbs

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.*

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
        get("/v2/posts/{post_id}/users") {
            call.parameters["post_id"]?.toIntOrNull()?.let { id ->
                val json = buildJsonObject {
                    val items = DatabaseFactory
                        .getPostUsers(id)
                        ?.map { Json.encodeToJsonElement(it) }

                    put("items", JsonArray(items ?: emptyList()))
                }

                call.respondText(json.toString())
            } ?: run {
                call.respond(HttpStatusCode.BadRequest, "Invalid post")
            }
        }

        get("/v2/users/{user_id}/friends") {
            call.parameters["user_id"]?.toIntOrNull()?.let { id ->
                val json = buildJsonObject {
                    val items = DatabaseFactory
                        .getUserFriends(id)
                        ?.map { Json.encodeToJsonElement(it) }

                    put("items", JsonArray(items ?: emptyList()))
                }

                call.respondText(json.toString())
            } ?: run {
                call.respond(HttpStatusCode.BadRequest, "Invalid user")
            }
        }

        get("/v2/tags/{tagname}/stats") {

        }

        get("/v2/posts") {
            // duration (minutes), limit

            // limit, query
        }
    }
}