package dev.lyric.income.sell.api

import dev.lyric.income.sell.api.provider.SellProvider
import io.papermc.paper.persistence.PersistentDataContainerView
import org.bukkit.NamespacedKey
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object IncomeSellAPI {

	// ---------------------------------------------------------
	// Provider Registry
	// ---------------------------------------------------------

	private val providers = mutableMapOf<String, SellProvider>()

	@JvmStatic
	fun registerProvider(provider: SellProvider) {
		providers[provider.identifier] = provider
	}

	@JvmStatic
	fun hasProvider(key: String) = providers.containsKey(key)

	@JvmStatic
	fun getProvider(identifier: String): SellProvider? = providers[identifier]

	// ---------------------------------------------------------
	// PDC Keys
	// ---------------------------------------------------------

	private val sellActionsKey = NamespacedKey.fromString("incomesell:sell_actions")!!

	// ---------------------------------------------------------
	// Handle Sell Actions
	// ---------------------------------------------------------

	@JvmStatic
	fun hasSellActions(item: ItemStack) =
		item.persistentDataContainer.has(sellActionsKey, PersistentDataType.TAG_CONTAINER)

	@JvmStatic
	fun addSellAction(
		item: ItemStack,
		providerId: String,
		amount: Double,
		argument: String?,
		overwrite: Boolean
	): ItemStack {
		val itemMeta = item.itemMeta ?: return item
		val rootPDC = itemMeta.persistentDataContainer

		val section = getOrCreateSection(rootPDC)
		val key =
			if (argument.isNullOrBlank()) providerId.lowercase() else "${providerId.lowercase()}/${argument.lowercase()}"
		if (overwrite) {
			section[key] = amount
		} else {
			section[key] = amount.plus(section.getOrDefault(key, 0.0))
		}
		saveSection(rootPDC, section)
		item.itemMeta = itemMeta
		return item
	}

	fun addSellAction(
		item: ItemStack,
		providerId: String,
		amount: Double,
		overwrite: Boolean
	): ItemStack {
		return addSellAction(item, providerId, amount, argument = null, overwrite = overwrite)
	}

	fun addSellAction(
		item: ItemStack,
		providerId: String,
		amount: Double,
		argument: String
	): ItemStack {
		return addSellAction(item, providerId, amount, argument = argument, overwrite = false)
	}

	fun addSellAction(
		item: ItemStack,
		providerId: String,
		amount: Double
	): ItemStack {
		return addSellAction(item, providerId, amount, argument = null, overwrite = false)
	}

	@JvmStatic
	fun createEmptySellResult(): SellResult = SellResult()

	@JvmStatic
	fun sellItem(item: ItemStack, result: SellResult): SellResult {
		if (!item.isEmpty && hasSellActions(item)) {
			recordItemTransaction(item.clone(), result)
			item.amount = 0
		}
		return result
	}

	@JvmStatic
	fun sellItem(item: ItemStack): SellResult {
		return sellItem(item, createEmptySellResult())
	}

	@JvmStatic
	fun sellInventory(inventory: Inventory, result: SellResult): SellResult {
		for (item in inventory.iterator()) {
			if (item != null && !item.isEmpty && hasSellActions(item)) {
				recordItemTransaction(item.clone(), result)
				item.amount = 0
			}
		}
		return result
	}

	@JvmStatic
	fun sellInventory(inventory: Inventory): SellResult {
		return sellInventory(inventory, createEmptySellResult())
	}

	@JvmStatic
	fun readSellActions(item: ItemStack): Map<String, Double> = getOrCreateSection(item.persistentDataContainer)

	private fun recordItemTransaction(item: ItemStack, sellResult: SellResult) {
		val actions = readSellActions(item)
		if (actions.isEmpty()) return
		sellResult.recordItemTransaction(item, item.amount, actions)
	}

	// ---------------------------------------------------------
	// Check sellable & add sell actions
	// ---------------------------------------------------------

	private fun getOrCreateSection(container: PersistentDataContainerView): MutableMap<String, Double> {
		return getSection(container)?.toMutableMap() ?: linkedMapOf()
	}

	private fun getSection(container: PersistentDataContainerView): Map<String, Double>? {
		val sellActions = container.get(sellActionsKey, PersistentDataType.TAG_CONTAINER) ?: return null
		val resultMap = linkedMapOf<String, Double>()
		for (key in sellActions.keys) {
			val amount = sellActions.get(key, PersistentDataType.DOUBLE) ?: continue
			resultMap[key.key] = amount
		}
		return resultMap
	}

	private fun saveSection(container: PersistentDataContainer, section: Map<String, Double>) {
		val sellActions = container.adapterContext.newPersistentDataContainer()
		for ((id, amount) in section) {
			val key = NamespacedKey.fromString("incomesell:$id")!!
			sellActions.set(key, PersistentDataType.DOUBLE, amount)
		}
		container.set(sellActionsKey, PersistentDataType.TAG_CONTAINER, sellActions)
	}

}