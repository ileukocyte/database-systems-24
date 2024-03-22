package io.ileukocyte.dbs.entities.v3

import io.ileukocyte.dbs.entities.TimestampSerializer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import java.sql.Timestamp

@Serializable
data class Achievement(
    val id: Int,
    val title: String?,
    val type: Type,
    @[SerialName("created_at") Serializable(with = TimestampSerializer::class)]
    val createdAt: Timestamp,
    val position: UInt
) {
    @Serializable
    enum class Type {
        @SerialName("badge")
        BADGE,
        @SerialName("post")
        POST,
    }
}