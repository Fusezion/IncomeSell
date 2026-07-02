package dev.lyric.income.sell.api

import dev.lyric.income.economy.api.EconomyAPI
import dev.lyric.income.economy.api.EconomyCollection
import dev.lyric.income.economy.api.EconomyEntry
import dev.lyric.income.economy.api.ProviderKey
import dev.lyric.income.sell.api.event.PlayerSellEvent
import dev.lyric.income.sell.api.event.PrePlayerSellEvent
import dev.lyric.income.sell.api.multiplier.MultiplierProviderRegistry
import dev.lyric.income.sell.utils.PDCKeys
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object IncomeSellAPI {

	//region Sell Actions/Item Modifications
	/**
	 *
	 * ##### Important
	 * This mutates the [item] passed through, if you don't want it mutated ensure you pass in a copy
	 * and use the resulting item stack otherwise you're free to ignore the returned output
	 *
	 * @param[item] The item that will have the sell data attached
	 * @param[collection] An economy collection consisting of the sell data to add
	 *
	 * @return The newly edited item stack, updated with the new sell data
	 *
	 */
	@JvmStatic
	fun addSellData(item: ItemStack, collection: EconomyCollection): ItemStack {
		item.editPersistentDataContainer { container ->
			val sellActions = container.getOrDefault(
				PDCKeys.sellActions,
				PersistentDataType.TAG_CONTAINER,
				container.adapterContext.newPersistentDataContainer()
			)
			editSellActions(sellActions, collection)
			if (sellActions.isEmpty) return@editPersistentDataContainer
			container.set(PDCKeys.sellActions, PersistentDataType.TAG_CONTAINER, sellActions)
		}
		return item
	}

	/**
	 *
	 * ##### Important
	 * This mutates the [item] passed through, if you don't want it mutated ensure you pass in a copy
	 * and use the resulting item stack otherwise you're free to ignore the returned output
	 *
	 * @param[item] The item that will have the sell data attached
	 * @param[entry] The entry consisting of the sell data to add
	 *
	 * @return The newly edited item stack, updated with the newly added sell data
	 *
	 */
	@JvmStatic
	fun addSellData(item: ItemStack, entry: EconomyEntry): ItemStack {
		val providerKey = entry.key
		return addSellData(item, providerKey.provider, providerKey.argument, entry.amount)
	}

	/**
	 *
	 * ##### Important
	 * This mutates the [item] passed through, if you don't want it mutated ensure you pass in a copy
	 * and use the resulting item stack otherwise you're free to ignore the returned output
	 *
	 * @param[item] The item that will have the sell data attached
	 * @param[providerKey]
	 * @param[amount]
	 *
	 * @return The newly edited item stack, updated with the newly add sell data
	 *
	 */
	@JvmStatic
	fun addSellData(item: ItemStack, providerKey: ProviderKey, amount: Double): ItemStack {
		return addSellData(item, providerKey.provider, providerKey.argument, amount)
	}

	@JvmStatic
	fun addSellData(item: ItemStack, providerId: String, argument: String?, amount: Double): ItemStack {
		item.editPersistentDataContainer { container ->
			val sellActions = container.getOrDefault(
				PDCKeys.sellActions,
				PersistentDataType.TAG_CONTAINER,
				container.adapterContext.newPersistentDataContainer()
			)
			val provider = EconomyAPI.getProvider(providerId) ?: return@editPersistentDataContainer
			if (argument != null && !provider.validateArgument(argument)) return@editPersistentDataContainer
			val namespacedKey = getNamespacedKey(providerId, argument) ?: return@editPersistentDataContainer
			sellActions.set(namespacedKey, PersistentDataType.DOUBLE, amount)
			container.set(PDCKeys.sellActions, PersistentDataType.TAG_CONTAINER, sellActions)
		}
		return item
	}

	@JvmStatic
	fun setSellData(item: ItemStack, collection: EconomyCollection): ItemStack {
		item.editPersistentDataContainer { container ->
			val sellActions = container.adapterContext.newPersistentDataContainer()
			editSellActions(sellActions, collection)
			if (sellActions.isEmpty) return@editPersistentDataContainer
			container.set(PDCKeys.sellActions, PersistentDataType.TAG_CONTAINER, sellActions)
		}
		return item
	}

	@JvmStatic
	fun clearSellData(item: ItemStack): ItemStack {
		item.editPersistentDataContainer { it.remove(PDCKeys.sellActions) }
		return item
	}

	@JvmStatic
	fun hasSellData(item: ItemStack): Boolean {
		return !item.isEmpty && item.persistentDataContainer.has(PDCKeys.sellActions)
	}

	@JvmStatic
	fun getSellData(item: ItemStack): EconomyCollection? {
		if (!hasSellData(item)) return null
		val container =
			item.persistentDataContainer.get(PDCKeys.sellActions, PersistentDataType.TAG_CONTAINER) ?: return null
		val entries = mutableListOf<EconomyEntry>()
		val itemAmount = item.amount
		for (key in container.keys) {
			val providerKeyString = key.key.split("/", limit = 2)
			val providerId = providerKeyString.first()
			val argument = providerKeyString.getOrNull(1)
			val provider = EconomyAPI.getProvider(providerId) ?: continue
			if (argument != null && !provider.validateArgument(argument)) continue
			val amount = container.get(key, PersistentDataType.DOUBLE) ?: continue
			entries += EconomyEntry(ProviderKey(providerId, argument), (amount * itemAmount))
		}
		return EconomyCollection(entries)
	}
	//endregion

	//region Sell Result
	@JvmStatic
	fun createSellReport(): SellReport = SellReport()

	fun processSell(player: Player, sellReport: SellReport): Boolean {
		val prePlayerSell = PrePlayerSellEvent(player, sellReport).callEvent()
		if (!prePlayerSell) return false
		MultiplierProviderRegistry.getProviders().forEach { multiplierProvider ->
			if (multiplierProvider.canApply(player))
				multiplierProvider.applyMultipliers(player, sellReport)
		}
		sellReport.getTransactions().map { entry ->
			for (multiplier in sellReport.getMultipliers().filter { it.canApplyTo(entry) }) {
				multiplier.applyTo(entry)
			}
			return@map entry
		}.let { EconomyCollection(it).deposit(player) }
		sellReport.markCompleted()
		PlayerSellEvent(player, sellReport).callEvent()
		return true
	}

	//endregion

	//region Utility Private Methods
	private fun getNamespacedKey(providerId: String, argument: String?): NamespacedKey? {
		val key = if (argument != null) "$providerId/$argument" else providerId
		return NamespacedKey.fromString("incomesell:$key")
	}

	private fun editSellActions(
		container: PersistentDataContainer,
		collection: EconomyCollection
	): PersistentDataContainer {
		for ((providerKey, amount) in collection.getCompactedEntries()) {
			val (providerId, argument) = providerKey
			val provider = EconomyAPI.getProvider(providerId) ?: continue
			if (argument != null && !provider.validateArgument(argument)) continue
			val namespacedKey = getNamespacedKey(providerId, argument) ?: continue
			container.set(namespacedKey, PersistentDataType.DOUBLE, amount)
		}
		return container
	}
	//endregion

}
