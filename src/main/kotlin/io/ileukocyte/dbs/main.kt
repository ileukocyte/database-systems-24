package io.ileukocyte.dbs

import io.ileukocyte.dbs.entities.v2.*
import io.ileukocyte.dbs.entities.v3.*

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
        // #1
        // Example: user_id = 120
        get("/v3/users/{user_id}/badge_history") {
            call.parameters["user_id"]?.toIntOrNull()?.let { id ->
                val output = DatabaseFactory.getBadgeHistory(id)
                val json = prettyJson.encodeToString(
                    MapSerializer(String.serializer(), ListSerializer(Achievement.serializer())),
                    mapOf("items" to output)
                )

                call.respondText(json, ContentType.Application.Json)
            } ?: run {
                call.respond(HttpStatusCode.BadRequest, "Invalid user")
            }
        }

        // #2
        // Example: tag = networking, count = 40
        get("/v3/tags/{tag}/comments") { // ?count=
            call.parameters["tag"]?.let { tag ->
                call.request.queryParameters["count"]?.toUIntOrNull()?.let { count ->
                    val output = DatabaseFactory.getPostsByComments(tag, count)
                    val json = prettyJson.encodeToString(
                        MapSerializer(String.serializer(), ListSerializer(CommentedPost.serializer())),
                        mapOf("result" to output)
                    )

                    call.respondText(json, ContentType.Application.Json)
                } ?: run {
                    call.respond(HttpStatusCode.BadRequest, "Invalid count")
                }
            }
        }

        // #3
        // Example: tag = linux, position = 2, limit = 1
        get("/v3/tags/{tag}/comments/{position}") { // ?limit=
            call.parameters["tag"]?.let { tag ->
                call.parameters["position"]?.toUIntOrNull()?.let { position ->
                    val limit = call.request.queryParameters["limit"]?.toUIntOrNull()
                    val output = DatabaseFactory.getTaggedPostComments(tag, position, limit)
                    val json = prettyJson.encodeToString(
                        MapSerializer(String.serializer(), ListSerializer(TaggedPostComment.serializer())),
                        mapOf("result" to output)
                    )

                    call.respondText(json, ContentType.Application.Json)
                } ?: run {
                    call.respond(HttpStatusCode.BadRequest, "Invalid position")
                }
            }
        }

        // #4
        // Example: post_id = 2154, limit = 2
        get("/v3/posts/{post_id}") { // ?limit=
            call.parameters["post_id"]?.toIntOrNull()?.let { id ->
                val limit = call.request.queryParameters["limit"]?.toUIntOrNull()
                val output = DatabaseFactory.getPostThread(id, limit)
                val json = prettyJson.encodeToString(
                    MapSerializer(String.serializer(), ListSerializer(ThreadPost.serializer())),
                    mapOf("result" to output)
                )

                call.respondText(json, ContentType.Application.Json)
            } ?: run {
                call.respond(HttpStatusCode.BadRequest, "Invalid post")
            }
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