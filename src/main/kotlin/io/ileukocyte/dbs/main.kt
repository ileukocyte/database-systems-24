package io.ileukocyte.dbs

import io.ileukocyte.dbs.entities.ClosedPost
import io.ileukocyte.dbs.entities.Post
import io.ileukocyte.dbs.entities.User

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
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
    val prettyJson = Json {
        prettyPrint = true
    }

    routing {
        // #1
        get("/v2/posts/{post_id}/users") {
            call.parameters["post_id"]?.toIntOrNull()?.let { id ->
                val output = DatabaseFactory.getPostUsers(id)
                val json = prettyJson.encodeToString(
                    MapSerializer(String.serializer(), ListSerializer(User.serializer())),
                    mapOf("items" to output)
                )

                call.respondText(json)
            } ?: run {
                call.respond(HttpStatusCode.BadRequest, "Invalid post")
            }
        }

        // #2
        get("/v2/users/{user_id}/friends") {
            call.parameters["user_id"]?.toIntOrNull()?.let { id ->
                val output = DatabaseFactory.getUserFriends(id)
                val json = prettyJson.encodeToString(
                    MapSerializer(String.serializer(), ListSerializer(User.serializer())),
                    mapOf("items" to output)
                )

                call.respondText(json)
            } ?: run {
                call.respond(HttpStatusCode.BadRequest, "Invalid user")
            }
        }

        // #3
        get("/v2/tags/{tagname}/stats") {
            call.parameters["tagname"]?.let { tag ->
                val output = DatabaseFactory.getTagStats(tag)
                val json = prettyJson.encodeToString(
                    MapSerializer(String.serializer(), MapSerializer(String.serializer(), Double.serializer())),
                    mapOf("result" to output)
                )

                call.respondText(json)
            }
        }

        get("/v2/posts") {
            val queryParams = call.request.queryParameters

            if (queryParams["duration"]?.toIntOrNull() != null) {
                // #4
                val duration = queryParams["duration"]?.toInt() ?: return@get
                val limit = queryParams["limit"]?.toIntOrNull()

                val output = DatabaseFactory.getPostsByDuration(duration, limit)
                val json = prettyJson.encodeToString(
                    MapSerializer(String.serializer(), ListSerializer(ClosedPost.serializer())),
                    mapOf("items" to output)
                )

                call.respondText(json)
            } else if (!queryParams["query"].isNullOrEmpty()) {
                // #5
                val query = queryParams["query"] ?: return@get
                val limit = queryParams["limit"]?.toIntOrNull()

                val output = DatabaseFactory.searchPosts(query, limit)
                val json = prettyJson.encodeToString(
                    MapSerializer(String.serializer(), ListSerializer(Post.serializer())),
                    mapOf("items" to output)
                )

                call.respondText(json)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters")
            }
        }
    }
}