package dev.lyric.income.sell.config.data

import dev.lyric.income.sell.config.serializers.BukkitColorSerializer
import io.papermc.paper.datacomponent.item.CustomModelData
import kotlinx.serialization.Serializable
import org.bukkit.Color

@Serializable
data class CustomModelDataConfig(
	val colors: List<@Serializable(BukkitColorSerializer::class) Color> = emptyList(),
	val strings: List<String> = emptyList(),
	val flags: List<Boolean> = emptyList(),
	val floats: List<Float> = emptyList()
) {
	fun toCustomModelData(): CustomModelData {
		return CustomModelData.customModelData().addColors(colors).addStrings(strings).addFlags(flags).addFloats(floats)
			.build()
	}
}