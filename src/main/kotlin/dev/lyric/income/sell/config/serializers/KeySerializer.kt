package dev.lyric.income.sell.config.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.key.Key

object KeySerializer : KSerializer<Key> {

	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Key", PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: Key) {
		encoder.encodeString(value.asMinimalString())
	}

	override fun deserialize(decoder: Decoder): Key {
		val keyString = decoder.decodeString().lowercase().replace(" ", "_")
		if (!Key.parseable(keyString)) throw SerializationException("Unable to parse key: $keyString")
		return Key.key(keyString)
	}

}