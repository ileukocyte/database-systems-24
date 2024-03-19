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
        //// Assignment #3
        // #1 120
        get("/v3/users/{user_id}/badge_history") {

        }

        // #2 networking, 40
        get("/v3/tags/{tag}/comments") {
            // ?count=
        }

        // #3 linux, 2, 1
        get("/v3/tags/{tag}/comments/{position}") {
            // ?limit=
        }

        // #4 2154, 2
        get("/v3/posts/{post_id}") {
            // ?limit=
        }

        //// Assignment #2
        // #1
        get("/v2/posts/{post_id}/users") {
            call.parameters["post_id"]?.toIntOrNull()?.let { id ->
                val output = DatabaseFactory.getPostUsers(id)
                val json = prettyJson.encodeToString(
                    MapSerializer(String.serializer(), ListSerializer(User.serializer())),
                    mapOf("items" to output)
                )

                call.respondText(json, ContentType.Application.Json)
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

                call.respondText(json, ContentType.Application.Json)
            } ?: run {
                call.respond(HttpStatusCode.BadRequest, "Invalid user")
            }
        }

        // #3
        get("/v2/tags/{tag}/stats") {
            call.parameters["tag"]?.let { tag ->
                val output = DatabaseFactory.getTagStats(tag)
                val json = prettyJson.encodeToString(
                    MapSerializer(String.serializer(), MapSerializer(String.serializer(), Double.serializer())),
                    mapOf("result" to output)
                )

                call.respondText(json, ContentType.Application.Json)
            }
        }

        get("/v2/posts") {
            val queryParams = call.request.queryParameters

            if (queryParams["duration"]?.toUIntOrNull() != null) {
                // #4
                val duration = queryParams["duration"]?.toUInt() ?: return@get
                val limit = queryParams["limit"]?.toUIntOrNull()

                val output = DatabaseFactory.getPostsByDuration(duration, limit)
                val json = prettyJson.encodeToString(
                    MapSerializer(String.serializer(), ListSerializer(ClosedPost.serializer())),
                    mapOf("items" to output)
                )

                call.respondText(json, ContentType.Application.Json)
            } else if (!queryParams["query"].isNullOrEmpty()) {
                // #5
                val query = queryParams["query"] ?: return@get
                val limit = queryParams["limit"]?.toUIntOrNull()

                val output = DatabaseFactory.searchPosts(query, limit)
                val json = prettyJson.encodeToString(
                    MapSerializer(String.serializer(), ListSerializer(Post.serializer())),
                    mapOf("items" to output)
                )

                call.respondText(json, ContentType.Application.Json)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters")
            }
        }
    }
}