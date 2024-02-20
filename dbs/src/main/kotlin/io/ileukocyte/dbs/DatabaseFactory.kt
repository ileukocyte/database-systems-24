package io.ileukocyte.dbs

import kotlinx.coroutines.Dispatchers

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
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

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    fun getDatabaseVersion(): String? {
        return transaction {
            exec("SELECT version();") { rs ->
                if (rs.next()) {
                    rs.getString(1)
                } else {
                    ""
                }
            }
        }
    }
}