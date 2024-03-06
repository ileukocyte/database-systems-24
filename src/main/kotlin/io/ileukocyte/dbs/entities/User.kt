package io.ileukocyte.dbs.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import java.sql.Timestamp

@Serializable
data class User(
    val id: Int,
    val reputation: Int,
    @[SerialName("creationdate") Serializable(with = TimestampSerializer::class)] val creationDate: Timestamp,
    @SerialName("displayname") val displayName: String,
    @[SerialName("lastaccessdate") Serializable(with = TimestampSerializer::class)] val lastAccessDate: Timestamp?,
    @SerialName("websiteurl") val websiteUrl: String?,
    val location: String?,
    @SerialName("aboutme") val aboutMe: String?,
    val views: Int,
    val upvotes: Int,
    val downvotes: Int,
    @SerialName("profileimageurl") val profileImageUrl: String?,
    val age: Int?,
    @SerialName("accountid") val accountId: Int?
)