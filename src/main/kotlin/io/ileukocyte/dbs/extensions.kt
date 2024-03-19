package io.ileukocyte.dbs

import java.sql.ResultSet

fun <T> ResultSet.asList(extract: ResultSet.() -> T) = buildList {
    while (next()) {
        add(this@asList.extract())
    }
}