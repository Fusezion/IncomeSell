package dev.lyric.income.sell.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SellGuiConfig(
	@SerialName("inventory-name")
	val inventoryName: String,
	val shape: List<String>,
	@SerialName("sell-action-slot")
	val sellActionSlot: Char,
	val items: Map<Char, ItemStackConfig>
)
