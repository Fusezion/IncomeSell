package dev.lyric.income.sell.config.serializers

import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object DataComponentTypeSerializer : KSerializer<DataComponentType> {

	private val dataComponentTypes = RegistryAccess.registryAccess().getRegistry(RegistryKey.DATA_COMPONENT_TYPE)
	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DataComponentType", PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: DataComponentType) {
		encoder.encodeSerializableValue(KeySerializer, value.key)
	}

	override fun deserialize(decoder: Decoder): DataComponentType {
		val key = decoder.decodeSerializableValue(KeySerializer)
		return dataComponentTypes[key] ?: throw SerializationException("Unknown DataComponentType key: $key")
	}
}