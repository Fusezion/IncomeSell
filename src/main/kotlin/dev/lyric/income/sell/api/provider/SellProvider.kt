package dev.lyric.income.sell.api.provider

import org.bukkit.entity.Player

interface SellProvider {

	val identifier: String
	val displayName: String

	fun handleSell(player: Player, extraArg: String?, amount: Double)

	fun displayString(extraArg: String?, amount: Double): String

	fun displayArgument(extraArg: String?): String? = null

	fun acceptsMultipliers(extraArg: String?): Boolean = true

}