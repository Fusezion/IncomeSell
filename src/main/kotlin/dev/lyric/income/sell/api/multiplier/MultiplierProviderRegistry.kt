package dev.lyric.income.sell.api.multiplier

object MultiplierProviderRegistry {

	private val providers = mutableMapOf<String, MultiplierProvider>()

	fun getProvider(key: String): MultiplierProvider? = providers[key]

	fun hasProvider(key: String) = providers.containsKey(key)

	fun registerProvider(provider: MultiplierProvider) {
		providers[provider.key] = provider
	}

	fun unregisterProvider(key: String) = providers.remove(key)

	fun getProviders() = providers.values.toList()

}