package dev.lyric.income.sell.api

import dev.lyric.income.sell.api.provider.SellProvider

object SellProviderRegistry {

	private val providers = mutableMapOf<String, SellProvider>()

	@JvmStatic
	fun getProvider(identifier: String): SellProvider? = providers[identifier]

	@JvmStatic
	fun hasProvider(identifier: String) = providers.containsKey(identifier)

	@JvmStatic
	fun register(sellProvider: SellProvider) {
		providers[sellProvider.identifier] = sellProvider
	}

}