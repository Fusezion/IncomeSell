package dev.lyric.income.sell.config.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Color as BukkitColor

object BukkitColorSerializer : KSerializer<BukkitColor> {

	override val descriptor = ListSerializer(Int.serializer()).descriptor

	override fun serialize(encoder: Encoder, value: BukkitColor) {
		encoder.encodeSerializableValue(ListSerializer(Int.serializer()), listOf(value.red, value.green, value.blue))
	}

	override fun deserialize(decoder: Decoder): BukkitColor {
		val values = decoder.decodeSerializableValue(ListSerializer(Int.serializer()))
		return BukkitColor.fromRGB(values[0], values[1], values[2])
	}
}