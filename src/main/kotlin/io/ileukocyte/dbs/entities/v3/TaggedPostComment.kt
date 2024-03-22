package io.ileukocyte.dbs.entities.v3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaggedPostComment(
    val id: Int,
    @SerialName("displayname")
    val displayName: String?,
    val body: String,
    val text: String,
    val score: Int,
    val position: UInt
)
