package dev.lyric.income.sell.config

import dev.lyric.income.sell.config.data.SellActionSlotsConfig
import dev.lyric.income.sell.config.data.ItemStackConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SellGuiConfig(
	val title: String,
	val layout: List<String>,
	@SerialName("sell-action-slots")
	val sellActionSlots: SellActionSlotsConfig,
	val items: Map<Char, ItemStackConfig>
)
