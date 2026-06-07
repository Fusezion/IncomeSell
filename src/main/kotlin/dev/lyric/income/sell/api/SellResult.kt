package dev.lyric.income.sell.api

import dev.lyric.income.sell.api.IncomeSellAPI.getProviderAndArgument
import dev.lyric.income.sell.api.IncomeSellAPI.isValidProvider
import net.kyori.adventure.text.Component
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

class SellResult {

	// --------------------------------------------------
	// Transaction Records
	// --------------------------------------------------
	//region Transaction Fold Region

	private var transactions: MutableMap<String, Double> = mutableMapOf()
	private var itemTransactions: MutableMap<Component, ItemTransaction> = mutableMapOf()

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
		val itemTransaction = itemTransactions.computeIfAbsent(item.effectiveName()) { ItemTransaction(item) }
		itemTransaction.amountSold += itemAmount
		itemTransaction.transactions.merge(providerKey, (unitAmount * itemAmount), Double::plus)
	}

	fun recordItemTransaction(item: ItemStack, itemAmount: Int, transactions: Map<String, Double>) {
		val item = item.clone()
		val itemAmount = itemAmount.coerceAtLeast(1)
		val itemTransaction = itemTransactions.computeIfAbsent(item.effectiveName()) { ItemTransaction(item) }
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
	private var providerMultipliers: MutableMap<String, Float> = mutableMapOf()
	private var providerSpecificMultipliers: MutableMap<String, Float> = mutableMapOf()

	fun editGlobalMultiplier(multiplier: Float, overwrite: Boolean = false) {
		globalMultiplier = if (overwrite) multiplier else globalMultiplier * multiplier
	}

	fun editProviderMultiplier(providerKey: String, multiplier: Float, overwrite: Boolean = false) {
		if (!isValidProvider(providerKey)) return
		val (providerId, argument) = getProviderAndArgument(providerKey)
		val provider = SellProviderRegistry.getProvider(providerId) ?: return
		if (!provider.acceptsMultipliers(argument)) return
		if (argument == null) {
			if (overwrite) {
				providerMultipliers[providerId] = multiplier
			} else {
				providerMultipliers.merge(providerId, multiplier, Float::times)
			}
			return
		}
		if (overwrite) {
			providerSpecificMultipliers[providerKey] = multiplier
		} else {
			providerSpecificMultipliers.merge(providerKey, multiplier, Float::times)
		}
	}

	internal fun calculateMultiplier(providerKey: String): Float {
		if (!isValidProvider(providerKey)) return 0f
		val (providerId, argument) = getProviderAndArgument(providerKey)
		val provider = SellProviderRegistry.getProvider(providerId) ?: return 0f
		if (!provider.acceptsMultipliers(argument)) return 0f
		var multiplier = 1f
		if (provider.acceptsGlobalMultiplier())
			multiplier *= globalMultiplier
		if (provider.acceptsMultipliers(null))
			multiplier *= providerMultipliers.getOrDefault(providerKey, 1f)
		if (argument != null && provider.acceptsMultipliers(argument))
			multiplier *= providerSpecificMultipliers.getOrDefault(providerKey, 1f)
		return multiplier
	}

	fun getGlobalMultiplier() = globalMultiplier

	fun getProviderSpecificMultipliers() = providerSpecificMultipliers.toMap()

	fun getProviderMultipliers() = providerMultipliers.toMap()
	//endregion

	// --------------------------------------------------
	// Utility Methods
	// --------------------------------------------------
	//region Utility Fold Region

	fun hasMultipliers() = globalMultiplier != 1f || providerMultipliers.isNotEmpty() || providerSpecificMultipliers.isNotEmpty()

	fun hasItemTransactions() = (itemTransactions.isNotEmpty() && itemTransactions.values.any { it.transactions.isNotEmpty() })

	fun hasTransactions() = transactions.isNotEmpty() || hasItemTransactions()

	fun getTransactions() = transactions.toMap()

	fun getItemTransactions() = itemTransactions.values.toList()

	/**
	 * Returns the merged transaction history between normal transactions and item transactions.
	 * This is used in handling the payout but is also a semi-safe way of validating if the sell-result
	 * had valid information for payouts.
	 *
	 * **NOTE:** this means you should not call [recordTransaction] and [recordItemTransaction] together when recording items
	 */
	fun getMergedTransactions(): Map<String, Double> {
		val validTransactions = getTransactions().filter { isValidProvider(it.key) }.toMutableMap()
		val itemTransactions = getItemTransactions()
		for (itemTransaction in itemTransactions) {
			for ((providerKey, amount) in itemTransaction.transactions) {
				if (!isValidProvider(providerKey)) continue
				validTransactions.merge(providerKey, amount, Double::plus)
			}
		}
		return validTransactions
	}

	fun copy(): SellResult {
		val copy = SellResult()
		copy.transactions = transactions.toMutableMap()
		copy.itemTransactions = itemTransactions.toMutableMap()
		copy.globalMultiplier = globalMultiplier
		copy.providerMultipliers = providerMultipliers.toMutableMap()
		copy.providerSpecificMultipliers = providerSpecificMultipliers.toMutableMap()
		return copy
	}

	//endregion

}

