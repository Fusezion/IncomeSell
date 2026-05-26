package dev.lyric.income.sell.config.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import org.bukkit.Color as BukkitColor

object BukkitColorSerializer : KSerializer<BukkitColor> {

	@OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
	override val descriptor: SerialDescriptor = buildSerialDescriptor("BukkitColor", StructureKind.LIST)

	override fun serialize(encoder: Encoder, value: BukkitColor) {
		encoder.encodeStructure(descriptor) {
			encodeIntElement(descriptor, 0, value.red)
			encodeIntElement(descriptor, 1, value.green)
			encodeIntElement(descriptor, 2, value.blue)
		}
	}

	override fun deserialize(decoder: Decoder): BukkitColor {
		return decoder.decodeStructure(descriptor) {
			var red = 0
			var green = 0
			var blue = 0
			loop@ while (true) {
				when (val index = decodeElementIndex(descriptor)) {
					0 -> red = decodeIntElement(descriptor, 0)
					1 -> green = decodeIntElement(descriptor, 1)
					2 -> blue = decodeIntElement(descriptor, 2)
					CompositeDecoder.DECODE_DONE -> break@loop
					else -> throw SerializationException("Unexpected index: $index")
				}
			}
			return@decodeStructure BukkitColor.fromRGB(red, green, blue)
		}
	}
}