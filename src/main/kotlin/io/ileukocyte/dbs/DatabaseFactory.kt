package io.ileukocyte.dbs

import io.ileukocyte.dbs.entities.ClosedPost
import io.ileukocyte.dbs.entities.Post
import io.ileukocyte.dbs.entities.User

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

import java.sql.ResultSet

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
    fun getBadgeHistory(userId: Int): List<Any> {
        return transaction {
            val sqlQuery = """
                --
            """.trimIndent()

            exec(sqlQuery) { rs ->
                rs.asList {

                }
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
                WHERE comments.postid = $id
                ORDER BY comments.creationdate DESC;
            """.trimIndent()

            exec(sqlQuery) { rs ->
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
                    WHERE posts.owneruserid = $id
                    UNION
                    SELECT postid AS id
                    FROM comments
                    WHERE comments.userid = $id
                ) p ON comments.postid = p.id
                ORDER BY users.creationdate;
            """.trimIndent()

            exec(sqlQuery) { rs ->
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
                       ROUND(COUNT(*) FILTER (WHERE tagname = '$tag') * 100.0 / COUNT(DISTINCT posts.id), 2) AS ct
                FROM posts
                LEFT JOIN post_tags ON posts.id = post_tags.post_id
                LEFT JOIN tags ON post_tags.tag_id = tags.id
                GROUP BY dayofweek
                ORDER BY dayofweek;
            """.trimIndent()

            exec(sqlQuery) { rs ->
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
    fun getPostsByDuration(minutes: Int, limit: Int? = null): List<ClosedPost> {
        return transaction {
            val sqlQuery = """
                SELECT id, creationdate, viewcount, lasteditdate, lastactivitydate, title, closeddate,
                       ROUND(EXTRACT(EPOCH FROM (closeddate - creationdate)) / 60.0, 2) AS duration
                FROM posts
                WHERE closeddate IS NOT NULL AND ROUND(EXTRACT(EPOCH FROM (closeddate - creationdate)) / 60.0, 2) <= $minutes
                ORDER BY creationdate DESC${limit?.let { "\nLIMIT $it" }.orEmpty()};
            """.trimIndent()

            exec(sqlQuery) { rs ->
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
    fun searchPosts(query: String, limit: Int? = null): List<Post> {
        return transaction {
            val sqlQuery = """
                SELECT posts.id, creationdate, viewcount, lasteditdate, lastactivitydate,
                       title, body, answercount, closeddate,
                       COALESCE(ARRAY_AGG(tagname) FILTER (WHERE tagname IS NOT NULL), ARRAY[]::TEXT[]) AS tags
                FROM posts
                LEFT JOIN post_tags ON posts.id = post_tags.post_id
                LEFT JOIN tags ON post_tags.tag_id = tags.id
                WHERE UNACCENT(title) ILIKE UNACCENT('%$query%')
                      OR UNACCENT(body) ILIKE UNACCENT('%$query%')
                GROUP BY posts.id, creationdate
                ORDER BY creationdate DESC${limit?.let { "\nLIMIT $it" }.orEmpty()};
            """.trimIndent()

            exec(sqlQuery) { rs ->
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