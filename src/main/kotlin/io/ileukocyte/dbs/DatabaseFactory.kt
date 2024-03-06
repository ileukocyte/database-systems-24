package io.ileukocyte.dbs

import io.ileukocyte.dbs.entities.User
import kotlinx.coroutines.Dispatchers

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
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

    fun getPostUsers(id: Int): List<User>? {
        return transaction {
            val query = """
                SELECT users.id, reputation, users.creationdate,
                displayname, lastaccessdate, websiteurl,
                location, aboutme, users.views,
                upvotes, downvotes, profileimageurl,
                age, accountid
                FROM posts
                LEFT JOIN comments ON postid = posts.id
                JOIN users ON userid = users.id
                WHERE posts.id = $id
                ORDER BY comments.creationdate DESC
            """.trimIndent()

            exec(query) { rs ->
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
                        getInt("age"),
                        getInt("accountid")
                    )
                }
            }
        }
    }

    fun getUserFriends(id: Int): List<User>? {
        return transaction {
            val query = """
                SELECT DISTINCT users.id, reputation, users.creationdate,
                displayname, lastaccessdate, websiteurl,
                location, aboutme, users.views,
                upvotes, downvotes, profileimageurl,
                age, accountid
                FROM posts
                LEFT JOIN comments ON postid = posts.id
                JOIN users ON userid = users.id
                WHERE posts.owneruserid = $id
                ORDER BY users.creationdate;
            """.trimIndent()

            exec(query) { rs ->
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
                        getInt("age"),
                        getInt("accountid")
                    )
                }
            }
        }
    }

    private fun <T> ResultSet.asList(extract: ResultSet.() -> T) = buildList {
        while (next()) {
            add(this@asList.extract())
        }
    }
}