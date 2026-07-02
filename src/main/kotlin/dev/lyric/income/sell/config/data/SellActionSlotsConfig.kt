package dev.lyric.income.sell.config.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SellActionSlotsConfig(
	@SerialName("sell-action")
	val sellAction: Char,
	@SerialName("sellable-slot")
	val sellableSlot: Char
)
