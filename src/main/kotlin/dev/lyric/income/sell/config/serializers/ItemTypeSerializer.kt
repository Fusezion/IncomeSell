package dev.lyric.income.sell.config.serializers

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.inventory.ItemType

object ItemTypeSerializer : KSerializer<ItemType> {

	private val itemRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM)
	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ItemType", PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: ItemType) {
		encoder.encodeSerializableValue(KeySerializer, value.key)
	}

	override fun deserialize(decoder: Decoder): ItemType {
		val key = decoder.decodeSerializableValue(KeySerializer)
		return itemRegistry[key] ?: throw SerializationException("Unknown ItemType key: $key")
	}
}