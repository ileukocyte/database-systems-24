package io.ileukocyte.dbs.entities

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

import java.sql.Timestamp
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object TimestampSerializer : KSerializer<Timestamp> {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.Axxx")

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("java.sql.Timestamp", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Timestamp) {
        encoder.encodeString(formatter.format(OffsetDateTime.ofInstant(value.toInstant(), ZoneOffset.UTC)))
    }

    override fun deserialize(decoder: Decoder): Timestamp {
        return Timestamp.from(Instant.from(formatter.parse(decoder.decodeString())))
    }
}