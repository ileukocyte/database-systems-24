package io.ileukocyte.dbs.entities.v3

import io.ileukocyte.dbs.entities.TimestampSerializer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import java.sql.Timestamp

@Serializable
data class CommentedPost(
    @SerialName("post_id")
    val postId: Int,
    val title: String?,
    @SerialName("displayname")
    val displayName: String?,
    val text: String,
    @[SerialName("post_created_at") Serializable(with = TimestampSerializer::class)]
    val postCreatedAt: Timestamp,
    @[SerialName("created_at") Serializable(with = TimestampSerializer::class)]
    val createdAt: Timestamp,
    val diff: String,
    val avg: String
)