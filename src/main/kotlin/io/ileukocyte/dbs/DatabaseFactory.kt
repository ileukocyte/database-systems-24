package io.ileukocyte.dbs

import io.ileukocyte.dbs.entities.v2.*
import io.ileukocyte.dbs.entities.v3.*

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.UIntegerColumnType
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val dbName = System.getenv("DATABASE_NAME")
        val dbAddress = System.getenv("DATABASE_HOST")
        val dbPort = System.getenv("DATABASE_PORT").toInt()
        val dbUser = System.getenv("DATABASE_USER")
        val dbPass = System.getenv("DATABASE_PASSWORD")

        Database.connect(
            url = "jdbc:postgresql://$dbAddress:$dbPort/$dbName",
            driver = "org.postgresql.Driver",
            user = dbUser,
            password = dbPass
        )
    }

    //// Assignment #3
    // #1
    fun getBadgeHistory(userId: Int): List<Achievement> {
        return transaction {
            val sqlQuery = """
                --
            """.trimIndent()

            exec(sqlQuery, listOf(IntegerColumnType() to userId)) { rs ->
                rs.asList {

                }
                emptyList() // TODO
            }
        } ?: emptyList()
    }

    // #2
    fun getPostsByComments(tag: String, count: UInt): List<CommentedPost> {
        return transaction {
            val sqlQuery = """
                --
            """.trimIndent()

            exec(sqlQuery, listOf(TextColumnType() to tag, UIntegerColumnType() to count)) { rs ->
                rs.asList {

                }
                emptyList() // TODO
            }
        } ?: emptyList()
    }

    // #3
    fun getTaggedPostComments(
        tag: String,
        position: UInt,
        limit: UInt? = null
    ): List<TaggedPostComment> {
        return transaction {
            val sqlQuery = """
                SELECT comments.id, users.displayname, p.body, comments.text, comments.score,
                       ARRAY_POSITION(comment_ids, comments.id) AS position
                FROM comments
                JOIN users ON users.id = comments.userid
                JOIN (
                    SELECT posts.id AS post_id, posts.body,
                           ARRAY_AGG(comments.id ORDER BY comments.creationdate) AS comment_ids
                    FROM posts
                    JOIN post_tags ON post_tags.post_id = posts.id
                    JOIN tags ON post_tags.tag_id = tags.id
                    JOIN comments ON posts.id = comments.postid
                    WHERE tags.tagname = ?
                    GROUP BY posts.id, posts.body, posts.creationdate
                    HAVING COUNT(*) >= ?
                    ORDER BY posts.creationdate${limit?.let { "\nLIMIT ?" }.orEmpty()}
                ) p ON p.comment_ids[?] = comments.id;
            """.trimIndent()
            val params = listOfNotNull(
                TextColumnType() to tag,
                UIntegerColumnType() to position,
                limit?.let { UIntegerColumnType() to it },
                UIntegerColumnType() to position
            )

            exec(sqlQuery, params) { rs ->
                rs.asList {
                    TaggedPostComment(
                        rs.getInt("id"),
                        rs.getString("displayname"),
                        rs.getString("body"),
                        rs.getString("text"),
                        rs.getInt("score"),
                        rs.getInt("position").toUInt()
                    )
                }
            }
        } ?: emptyList()
    }

    // #4
    fun getPostThread(postId: Int, limit: UInt? = null): List<ThreadPost> {
        return transaction {
            val sqlQuery = """
                --
            """.trimIndent()
            val params = listOfNotNull(
                IntegerColumnType() to postId,
                limit?.let { UIntegerColumnType() to it }
            )

            exec(sqlQuery, params) { rs ->
                rs.asList {

                }
                emptyList() // TODO
            }
        } ?: emptyList()
    }

    //// Assignment #2
    // #1
    fun getPostUsers(id: Int): List<User> {
        return transaction {
            val sqlQuery = """
                SELECT users.id, reputation, users.creationdate,
                       displayname, lastaccessdate, websiteurl,
                       location, aboutme, users.views,
                       upvotes, downvotes, profileimageurl,
                       age, accountid
                FROM users
                JOIN comments ON comments.userid = users.id
                WHERE comments.postid = ?
                ORDER BY comments.creationdate DESC;
            """.trimIndent()

            exec(sqlQuery, listOf(IntegerColumnType() to id)) { rs ->
                rs.asList {
                    User(
                        getInt("id"),
                        getInt("reputation"),
                        getTimestamp("creationdate"),
                        getString("displayname"),
                        getTimestamp("lastaccessdate"),
                        getString("websiteurl")?.ifEmpty { null },
                        getString("location")?.ifEmpty { null },
                        getString("aboutme")?.ifEmpty { null },
                        getInt("views"),
                        getInt("upvotes"),
                        getInt("downvotes"),
                        getString("profileimageurl")?.ifEmpty { null },
                        getObject("age") as? Int,
                        getInt("accountid")
                    )
                }
            }
        } ?: emptyList()
    }

    // #2
    fun getUserFriends(id: Int): List<User> {
        return transaction {
            val sqlQuery = """
                SELECT DISTINCT users.id, reputation, users.creationdate,
                                displayname, lastaccessdate, websiteurl,
                                location, aboutme, users.views,
                                upvotes, downvotes, profileimageurl,
                                age, accountid
                FROM users
                JOIN comments ON comments.userid = users.id
                JOIN (
                    SELECT posts.id
                    FROM posts
                    WHERE posts.owneruserid = ?
                    UNION
                    SELECT postid AS id
                    FROM comments
                    WHERE comments.userid = ?
                ) p ON comments.postid = p.id
                ORDER BY users.creationdate;
            """.trimIndent()

            exec(sqlQuery, listOf(IntegerColumnType() to id, IntegerColumnType() to id)) { rs ->
                rs.asList {
                    User(
                        getInt("id"),
                        getInt("reputation"),
                        getTimestamp("creationdate"),
                        getString("displayname"),
                        getTimestamp("lastaccessdate"),
                        getString("websiteurl")?.ifEmpty { null },
                        getString("location")?.ifEmpty { null },
                        getString("aboutme")?.ifEmpty { null },
                        getInt("views"),
                        getInt("upvotes"),
                        getInt("downvotes"),
                        getString("profileimageurl")?.ifEmpty { null },
                        getObject("age") as? Int,
                        getInt("accountid")
                    )
                }
            }
        } ?: emptyList()
    }

    // #3
    fun getTagStats(tag: String): Map<String, Double> {
        return transaction {
            val sqlQuery = """
                SELECT EXTRACT(ISODOW FROM posts.creationdate AT TIME ZONE 'UTC') AS dayofweek,
                       ROUND(COUNT(*) FILTER (WHERE tagname = ?) * 100.0 / COUNT(DISTINCT posts.id), 2) AS ct
                FROM posts
                LEFT JOIN post_tags ON posts.id = post_tags.post_id
                LEFT JOIN tags ON post_tags.tag_id = tags.id
                GROUP BY dayofweek
                ORDER BY dayofweek;
            """.trimIndent()

            exec(sqlQuery, listOf(TextColumnType() to tag)) { rs ->
                rs.asList { getDouble("ct") }
            }?.let { counts ->
                val daysOfWeek = listOf(
                    "monday",
                    "tuesday",
                    "wednesday",
                    "thursday",
                    "friday",
                    "saturday",
                    "sunday"
                )

                daysOfWeek.associateWith { counts[daysOfWeek.indexOf(it)] }
            }
        } ?: emptyMap()
    }

    // #4
    fun getPostsByDuration(minutes: UInt, limit: UInt? = null): List<ClosedPost> {
        return transaction {
            val sqlQuery = """
                SELECT id, creationdate, viewcount, lasteditdate, lastactivitydate, title, closeddate,
                       ROUND(EXTRACT(EPOCH FROM (closeddate - creationdate)) / 60.0, 2) AS duration
                FROM posts
                WHERE closeddate IS NOT NULL AND ROUND(EXTRACT(EPOCH FROM (closeddate - creationdate)) / 60.0, 2) <= ?
                ORDER BY creationdate DESC${limit?.let { "\nLIMIT ?" }.orEmpty()};
            """.trimIndent()
            val params = listOfNotNull(
                UIntegerColumnType() to minutes,
                limit?.let { UIntegerColumnType() to it }
            )

            exec(sqlQuery, params) { rs ->
                rs.asList {
                    ClosedPost(
                        getInt("id"),
                        getTimestamp("creationdate"),
                        getInt("viewcount"),
                        getTimestamp("lasteditdate"),
                        getTimestamp("lastactivitydate"),
                        getString("title"),
                        getTimestamp("closeddate"),
                        getDouble("duration")
                    )
                }
            }
        } ?: emptyList()
    }

    // #5
    fun searchPosts(query: String, limit: UInt? = null): List<Post> {
        return transaction {
            val sqlQuery = """
                SELECT posts.id, creationdate, viewcount, lasteditdate, lastactivitydate,
                       title, body, answercount, closeddate,
                       COALESCE(ARRAY_AGG(tagname) FILTER (WHERE tagname IS NOT NULL), ARRAY[]::TEXT[]) AS tags
                FROM posts
                LEFT JOIN post_tags ON posts.id = post_tags.post_id
                LEFT JOIN tags ON post_tags.tag_id = tags.id
                WHERE UNACCENT(title) ILIKE UNACCENT(?)
                    OR UNACCENT(body) ILIKE UNACCENT(?)
                GROUP BY posts.id, creationdate
                ORDER BY creationdate DESC${limit?.let { "\nLIMIT ?" }.orEmpty()};
            """.trimIndent()
            val params = listOfNotNull(
                TextColumnType() to "%$query%",
                TextColumnType() to "%$query%",
                limit?.let { UIntegerColumnType() to it }
            )

            exec(sqlQuery, params) { rs ->
                rs.asList {
                    Post(
                        getInt("id"),
                        getTimestamp("creationdate"),
                        getInt("viewcount"),
                        getTimestamp("lasteditdate"),
                        getTimestamp("lastactivitydate"),
                        getString("title"),
                        getString("body"),
                        getInt("answercount"),
                        getTimestamp("closeddate"),
                        @Suppress("UNCHECKED_CAST")
                        (getArray("tags").array as? Array<String>)?.asList() ?: emptyList()
                    )
                }
            }
        } ?: emptyList()
    }
}