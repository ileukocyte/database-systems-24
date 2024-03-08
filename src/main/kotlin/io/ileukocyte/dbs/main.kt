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
        // #1
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

        // #2
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

        // #3
        get("/v2/tags/{tagname}/stats") {
            call.parameters["tagname"]?.let { tag ->
                val json = buildJsonObject {
                    val items = JsonObject(DatabaseFactory
                        .getTagStats(tag)
                        ?.map { (k, v) -> k to JsonPrimitive(v) }
                        ?.toMap() ?: emptyMap())

                    put("result", items)
                }

                call.respondText(json.toString())
            }
        }

        get("/v2/posts") {
            // #4
            // duration (minutes), limit

            // #5
            // limit, query
        }
    }
}