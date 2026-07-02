package dev.lyric.income.sell.multiplier.default

import dev.lyric.income.economy.api.EconomyAPI
import dev.lyric.income.economy.api.EconomyEntry
import dev.lyric.income.sell.multiplier.DisplayableMultiplier

data class DisplayableProviderMultiplier(
	override val multiplier: Float,
	val providerId: String,
	val argument: String?
) : DisplayableMultiplier {

	override val displayName: String
		get() {
			val provider = EconomyAPI.getProvider(providerId)!!
			if (argument == null) return provider.displayName
			return provider.argumentDisplayName(argument)
		}

	override fun canApplyTo(entry: EconomyEntry): Boolean {
		val (providerId, argument) = entry.key
		if (providerId != this.providerId) return false
		val provider = EconomyAPI.getProvider(providerId) ?: return false
		if (this.argument != null) {
			if (this.argument != argument) return false
			if (!provider.validateArgument(this.argument)) return false
		}
		return true
	}

	override fun applyTo(entry: EconomyEntry) {
		entry.amount *= multiplier
	}
}