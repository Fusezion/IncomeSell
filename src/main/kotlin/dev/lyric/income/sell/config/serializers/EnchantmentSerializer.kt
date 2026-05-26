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
import org.bukkit.enchantments.Enchantment

object EnchantmentSerializer : KSerializer<Enchantment> {

	val enchantments = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)

	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("enchantment", PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: Enchantment) {
		encoder.encodeSerializableValue(KeySerializer, value.key)
	}

	override fun deserialize(decoder: Decoder): Enchantment {
		val key = decoder.decodeSerializableValue(KeySerializer)
		return enchantments[key] ?: throw SerializationException("")
	}
}