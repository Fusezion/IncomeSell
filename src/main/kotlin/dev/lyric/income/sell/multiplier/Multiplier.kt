package dev.lyric.income.sell.multiplier

import dev.lyric.income.economy.api.EconomyEntry

interface Multiplier {
	val multiplier: Float

	fun canApplyTo(entry: EconomyEntry): Boolean

	fun applyTo(entry: EconomyEntry)
}

