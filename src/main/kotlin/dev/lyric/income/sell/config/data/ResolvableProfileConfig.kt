package dev.lyric.income.sell.config.data

import dev.lyric.income.sell.config.serializers.UUIDSerializer
import io.papermc.paper.datacomponent.item.ResolvableProfile
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ResolvableProfileConfig(
	val name: String? = null,
	@Serializable(with = UUIDSerializer::class) val uuid: UUID? = null,
) {
	fun toResolvableProfile(): ResolvableProfile {
		return ResolvableProfile.resolvableProfile().name(name).uuid(uuid).build()
	}
}