package dev.lyric.income.sell.multiplier.default

import dev.lyric.income.economy.api.EconomyEntry
import dev.lyric.income.sell.multiplier.DisplayableMultiplier

data class DisplayableSimpleMultiplier(
	override val multiplier: Float,
	override val displayName: String
) : DisplayableMultiplier {

	override fun canApplyTo(entry: EconomyEntry): Boolean = true

	override fun applyTo(entry: EconomyEntry) {
		entry.amount *= multiplier
	}
}
