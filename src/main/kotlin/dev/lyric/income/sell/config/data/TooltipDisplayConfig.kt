@file:Suppress("UnstableApiUsage")

package dev.lyric.income.sell.config.data

import dev.lyric.income.sell.config.serializers.DataComponentTypeSerializer
import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.item.TooltipDisplay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TooltipDisplayConfig(
	@SerialName("hide-tooltip") val hideTooltip: Boolean = false,
	@SerialName("hidden-components") val hiddenComponents: List<@Serializable(with = DataComponentTypeSerializer::class)DataComponentType> = emptyList()
) {
	fun toTooltipDisplay(): TooltipDisplay {
		return TooltipDisplay.tooltipDisplay().hideTooltip(hideTooltip).hiddenComponents(hiddenComponents.toSet())
			.build()
	}
}