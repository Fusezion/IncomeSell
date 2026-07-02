package dev.lyric.income.sell.api.multiplier

import dev.lyric.income.sell.api.SellReport
import org.bukkit.entity.Player

interface MultiplierProvider {

	val key: String
	val displayName: String

	fun canApply(player: Player): Boolean

	/**
	 * **NOTE:** A provider should never record anything besides a multiplier
	 */
	fun applyMultipliers(player: Player, sellReport: SellReport)

}