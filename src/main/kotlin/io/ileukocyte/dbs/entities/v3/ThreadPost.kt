package io.ileukocyte.dbs.entities.v3

import io.ileukocyte.dbs.entities.TimestampSerializer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import java.sql.Timestamp

@Serializable
data class ThreadPost(
    @SerialName("displayname")
    val displayName: String,
    val body: String,
    @[SerialName("created_at") Serializable(with = TimestampSerializer::class)]
    val createdAt: Timestamp
)
