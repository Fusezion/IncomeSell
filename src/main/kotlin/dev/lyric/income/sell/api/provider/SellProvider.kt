package dev.lyric.income.sell.api.provider

import org.bukkit.entity.Player
import java.text.NumberFormat
import java.text.NumberFormat.Style as NumberStyle

interface SellProvider {

	companion object {
		private val NUMBER_FORMAT = NumberFormat.getNumberInstance()
		private val COMPACT_NUMBER_FORMAT = NumberFormat.getCompactNumberInstance()
	}

	/**
	 * A lowercase alphanumeric identifier string used for sell-actions.
	 * This must follow the regex of `[a-z0-9._-]`
	 */
	val identifier: String

	/**
	 * The display name of this provider, this is used in a few places
	 * but most notably within the hover text for sell-multipliers
	 */
	val displayName: String

	/**
	 * The display name of an argument, this is used in a few places
	 * but most notably within the hover text for sell-multipliers
	 */
	fun argumentDisplayName(argument: String): String = argument

	/**
	 * Formats an amount of currency to be easier to read.
	 * By default, this implements the default java [NumberFormat] behavior
	 * Unless you have a special way you want things formatted it doesn't require being touched.
	 */
	fun formatAmount(amount: Double, argument: String?, formatStyle: NumberStyle = NumberStyle.LONG): String {
		return when (formatStyle) {
			NumberStyle.LONG -> NUMBER_FORMAT.format(amount)
			NumberStyle.SHORT -> COMPACT_NUMBER_FORMAT.format(amount)
		}
	}

	/**
	 * Handles the process of adding the currency to the supplied player.
	 */
	fun handleSell(player: Player, argument: String?, amount: Double)

	/**
	 * Returns whether the provider and its arguments accept being modified by multipliers
	 */
	fun acceptsMultipliers(argument: String?): Boolean = true

	fun isValidArgument(argument: String): Boolean = true

}