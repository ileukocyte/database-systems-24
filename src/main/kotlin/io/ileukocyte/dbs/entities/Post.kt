package io.ileukocyte.dbs.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import java.sql.Timestamp

@Serializable
data class Post(
    val id: Int,
    @[SerialName("creationdate") Serializable(with = TimestampSerializer::class)]
    val creationDate: Timestamp,
    @SerialName("viewcount")
    val viewCount: Int,
    @[SerialName("lasteditdate") Serializable(with = TimestampSerializer::class)]
    val lastEditDate: Timestamp?,
    @[SerialName("lastactivitydate") Serializable(with = TimestampSerializer::class)]
    val lastActivityDate: Timestamp?,
    val title: String?,
    val body: String,
    @SerialName("answercount")
    val answerCount: Int,
    @[SerialName("closeddate") Serializable(with = TimestampSerializer::class)]
    val closedDate: Timestamp?,
    val tags: List<String>
)