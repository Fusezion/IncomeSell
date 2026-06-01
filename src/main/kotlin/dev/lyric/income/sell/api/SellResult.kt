package dev.lyric.income.sell.api

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class SellResult {

	// --------------------------------------------------
	// Transaction Records
	// --------------------------------------------------
	//region Transaction Fold Region

	private val transactions: MutableMap<String, Double> = mutableMapOf()
	private val itemTransactions: MutableMap<ItemStackKey, ItemTransaction> = mutableMapOf()

	fun recordTransaction(providerKey: String, amount: Double) {
		if (!isValidProvider(providerKey)) return
		transactions.merge(providerKey, amount, Double::plus)
	}

	fun recordTransactions(transactions: Map<String, Double>) {
		transactions.forEach(::recordTransaction)
	}

	fun recordItemTransaction(item: ItemStack, itemAmount: Int, providerKey: String, unitAmount: Double) {
		val item = item.clone()
		if (!isValidProvider(providerKey)) return
		val itemAmount = itemAmount.coerceAtLeast(1)
		val itemTransaction = itemTransactions.computeIfAbsent(createItemStackKey(item.clone())) { ItemTransaction(item) }
		itemTransaction.amountSold += itemAmount
		itemTransaction.transactions.merge(providerKey, (unitAmount * itemAmount), Double::plus)
	}

	fun recordItemTransaction(item: ItemStack, itemAmount: Int, transactions: Map<String, Double>) {
		val item = item.clone()
		val itemAmount = itemAmount.coerceAtLeast(1)
		val itemTransaction = itemTransactions.computeIfAbsent(createItemStackKey(item.clone())) { ItemTransaction(item) }
		itemTransaction.amountSold += itemAmount
		for ((providerKey, unitAmount) in transactions) {
			if (!isValidProvider(providerKey)) continue
			itemTransaction.transactions.merge(providerKey, (unitAmount * itemAmount), Double::plus)
		}
	}

	fun recordInventoryTransaction(inventory: Inventory) {
		val sellableItems = inventory.storageContents.filterNotNull().filter(IncomeSellAPI::hasSellActions).map(ItemStack::clone)
		if (sellableItems.isEmpty()) return
		for (itemStack in sellableItems) {
			val sellActions = IncomeSellAPI.getSellActions(itemStack)
			if (sellActions.isEmpty()) continue
			recordItemTransaction(itemStack, itemStack.amount, sellActions)
		}
	}

	//endregion

	// --------------------------------------------------
	// Multipliers
	// --------------------------------------------------
	//region Multiplier Fold Region

	private var globalMultiplier: Float = 1f
	private val providerMultipliers: MutableMap<String, Float> = mutableMapOf()
	private val providerSpecificMultipliers: MutableMap<String, Float> = mutableMapOf()

	fun editGlobalMultiplier(multiplier: Float, overwrite: Boolean = false) {
		globalMultiplier = if (overwrite) multiplier else globalMultiplier * multiplier
	}

	fun editProviderMultiplier(providerKey: String, multiplier: Float, overwrite: Boolean = false) {
		if (!isValidProvider(providerKey)) return
		val (provider, argument) = getProviderAndArgument(providerKey)
		if (argument == null) {
			if (overwrite) providerMultipliers[provider] = multiplier
			else providerMultipliers.merge(provider, multiplier, Float::times)
			return
		}
		if (overwrite) providerSpecificMultipliers[providerKey] = multiplier
		else providerSpecificMultipliers.merge(providerKey, multiplier, Float::times)
	}

	private fun calculateMultiplier(providerKey: String): Float {
		if (!isValidProvider(providerKey)) return 0f
		var multiplier = globalMultiplier
		multiplier *= providerMultipliers.getOrDefault(providerKey, 1f)
		multiplier *= providerSpecificMultipliers.getOrDefault(providerKey, 1f)
		return multiplier
	}
	//endregion

	// --------------------------------------------------
	// Utility Methods
	// --------------------------------------------------
	//region Utility Fold Region

	fun hasTransactions() = transactions.isNotEmpty() || (itemTransactions.isNotEmpty() && itemTransactions.values.any { it.transactions.isNotEmpty() })

	fun getTransactions() = transactions.toMap()

	fun getItemTransactions() = itemTransactions.toMap()

	private fun getProviderAndArgument(providerKey: String): Pair<String, String?> {
		val parts = providerKey.split("/", limit = 2)
		val provider = parts[0]
		val argument = parts.getOrNull(1)
		return Pair(provider, argument)
	}

	private fun isValidProvider(providerKey: String): Boolean {
		val (providerId, argument) = getProviderAndArgument(providerKey)
		val provider = SellProviderRegistry.getProvider(providerId) ?: return false
		return argument == null || provider.isValidArgument(argument)
	}

	data class ItemStackKey(val itemStack: ItemStack)

	fun createItemStackKey(itemStack: ItemStack): ItemStackKey {
		val item = itemStack.clone()
		item.amount = 0
		return ItemStackKey(item)
	}

	//endregion

}

