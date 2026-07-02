package dev.lyric.income.sell.multiplier.default

import dev.lyric.income.economy.api.EconomyEntry
import dev.lyric.income.sell.multiplier.Multiplier

data class SimpleMultiplier(override val multiplier: Float) : Multiplier {

	override fun canApplyTo(entry: EconomyEntry): Boolean = true

	override fun applyTo(entry: EconomyEntry) {
		entry.amount *= multiplier
	}

}
