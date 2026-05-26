package dev.lyric.income.sell.api

import dev.lyric.income.sell.api.events.PlayerSellEvent
import dev.lyric.income.sell.utils.AdventureUtils.component
import dev.lyric.income.sell.utils.AdventureUtils.miniMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.text.NumberFormat

class SellResult {

	companion object {
		private val INTEGER_NUMBER_FORMAT = NumberFormat.getIntegerInstance()
		private val DECIMAL_NUMBER_FORMAT = NumberFormat.getNumberInstance()
	}

	// --------------------------------------------------
	// Transaction Records
	// --------------------------------------------------
	//region Transaction Fold Region

	private val transactions: MutableMap<String, Double> = mutableMapOf()
	private val itemTransactions: MutableMap<Component, ItemTransaction> = mutableMapOf()

	fun recordTransaction(providerKey: String, amount: Double) {
		val (provider, _) = getProviderKey(providerKey)
		if (!IncomeSellAPI.hasProvider(provider)) return
		transactions.merge(providerKey, amount, Double::plus)
	}

	fun recordTransactions(transactions: Map<String, Double>) {
		transactions.forEach(::recordTransaction)
	}

	fun recordItemTransaction(item: ItemStack, itemAmount: Int, providerKey: String, unitAmount: Double) {
		val itemAmount = itemAmount.coerceAtLeast(1)
		val itemTransaction = itemTransactions.computeIfAbsent(item.effectiveName()) { ItemTransaction(item) }
		itemTransaction.amountSold += itemAmount
		itemTransaction.transactions.merge(providerKey, (unitAmount * itemAmount), Double::plus)
	}

	fun recordItemTransaction(item: ItemStack, itemAmount: Int, transactions: Map<String, Double>) {
		val itemAmount = itemAmount.coerceAtLeast(1)
		val itemTransaction = itemTransactions.computeIfAbsent(item.effectiveName()) { ItemTransaction(item) }
		itemTransaction.amountSold += itemAmount
		for ((providerKey, unitAmount) in transactions) {
			itemTransaction.transactions.merge(providerKey, (unitAmount * itemAmount), Double::plus)
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

	fun editGlobalMultiplier(multiplier: Float) = editGlobalMultiplier(multiplier, false)

	fun editGlobalMultiplier(multiplier: Float, overwrite: Boolean) {
		globalMultiplier = if (overwrite) multiplier else globalMultiplier * multiplier
	}

	fun editProviderMultiplier(providerKey: String, multiplier: Float) {
		editProviderMultiplier(providerKey, multiplier, false)
	}

	fun editProviderMultiplier(providerKey: String, multiplier: Float, overwrite: Boolean) {
		val (provider, argument) = getProviderKey(providerKey)
		if (argument == null) {
			if (overwrite) providerMultipliers[provider] = multiplier
			else providerMultipliers.merge(provider, multiplier, Float::times)
			return
		}
		if (overwrite) providerSpecificMultipliers[providerKey] = multiplier
		else providerSpecificMultipliers.merge(providerKey, multiplier, Float::times)
	}

	private fun calculateMultiplier(providerKey: String): Float {
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

	fun handlePayout(player: Player): Boolean {
		if (!PlayerSellEvent(player, this).callEvent()) return false
		for ((providerKey, amount) in createMergedTransactionMap()) {
			val (providerId, argument) = getProviderKey(providerKey)
			val provider = IncomeSellAPI.getProvider(providerId) ?: continue
			var amount = amount
			if (provider.acceptsMultipliers(argument))
				amount *= calculateMultiplier(providerKey)
			if (amount <= 0) continue
			provider.handleSell(player, argument, amount)
		}
		return true
	}

	fun getSoldMessage() : Component {
		if (isEmpty()) return "You had nothing that was able to be sold".component(NamedTextColor.RED)
		val resolvers = mutableListOf<TagResolver>(
			Placeholder.component("total", formatTransactionMap(createMergedTransactionMap()) ?: "nothing".component()),
			Placeholder.component("breakdown", createBreakdownComponent())
		)
		if (itemTransactions.isNotEmpty()) {
			resolvers.add(Placeholder.unparsed("amount_sold", INTEGER_NUMBER_FORMAT.format(getTotalItemsSold())))
			return "&aSuccessfully sold &f<amount_sold>x&a items for &f<total>&a. <breakdown>".miniMessage(*resolvers.toTypedArray())
		}
		return "&aSuccessfully sold everything for &f<total>&a. <breakdown>".miniMessage(*resolvers.toTypedArray())
	}

	fun getTransactions() = transactions.toMap()

	fun getItemTransactions() = itemTransactions.values.toList()

	fun getTotalItemsSold() = itemTransactions.values.sumOf { it.amountSold }.coerceAtLeast(0)

	fun isEmpty() = transactions.isEmpty() && (itemTransactions.isEmpty() || itemTransactions.values.all { it.transactions.isEmpty() })

	private fun createMergedTransactionMap() : Map<String, Double> {
		val transactionMap = mutableMapOf<String, Double>()
		for ((providerKey, amount) in this.transactions) {
			transactionMap.merge(providerKey, amount, Double::plus)
		}
		for (itemTransaction in this.itemTransactions.values) {
			for ((providerKey, amount) in itemTransaction.transactions) {
				transactionMap.merge(providerKey, amount, Double::plus)
			}
		}
		return transactionMap
	}

	private fun createBreakdownComponent(): Component {
		val itemBreakdown = createItemBreakdown()
		val multiBreakdown = createMultiplierBreakdown()
		if (itemBreakdown == null && multiBreakdown == null) return Component.empty()

		var hoverComponent = Component.empty()
		if (multiBreakdown != null)
			hoverComponent = hoverComponent.append(multiBreakdown)
		if (multiBreakdown != null && itemBreakdown != null) hoverComponent = hoverComponent.appendNewline().appendNewline()
		if (itemBreakdown != null)
			hoverComponent = hoverComponent.append(itemBreakdown)
		return "(Hover)".component(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(hoverComponent))
	}

	private fun createItemBreakdown(): Component? {
		if (itemTransactions.isEmpty() || itemTransactions.values.all { it.transactions.isEmpty() }) return null
		val mappedItems = itemTransactions.values.mapNotNull(::formatItemTransaction).toMutableList()
		if (mappedItems.isEmpty()) return null
		mappedItems.addFirst("&8| &7<b>Item Breakdown</b>:".miniMessage())
		return mappedItems.reduceOrNull { acc, component -> acc.appendNewline().append(component) }
	}

	private fun createMultiplierBreakdown(): Component? {
		// Provider multiplier-map
		val providerMultiplierMap = mutableMapOf<String, MutableMap<String?, Float>>()
		for ((provider, multi) in providerMultipliers) {
			providerMultiplierMap.computeIfAbsent(provider) { mutableMapOf() }[null] = multi
		}
		for ((providerLey, multi) in providerSpecificMultipliers) {
			val (provider, argument) = getProviderKey(providerLey)
			providerMultiplierMap.computeIfAbsent(provider) { mutableMapOf() }[argument!!] = multi
		}
		// Escape early if multi will never give value
		if (globalMultiplier <= 0 || (providerMultiplierMap.isNotEmpty() && providerMultipliers.values.all { it <= 0 })) return null

		val multiplierBreakdown = mutableListOf("&8| &7<b>Multiplier Breakdown</b>:")
		if (globalMultiplier >= 0f)
			multiplierBreakdown.add("&8| &7Global: &f${DECIMAL_NUMBER_FORMAT.format(globalMultiplier)}x")

		for ((providerId, multiMap) in providerMultiplierMap) {
			val provider = IncomeSellAPI.getProvider(providerId) ?: continue
			val providerMulti = multiMap[null]
			if (providerMulti == null) {
				multiplierBreakdown.add("&8| &7${provider.displayName}:")
			} else {
				multiplierBreakdown.add("&8| &7${provider.displayName}: ${DECIMAL_NUMBER_FORMAT.format(providerMulti)}x")
			}
			for ((argument, argumentMulti) in multiMap) {
				val argumentString = provider.displayArgument(argument) ?: continue
				multiplierBreakdown.add("&8|   &7$argumentString: &f${DECIMAL_NUMBER_FORMAT.format(argumentMulti)}x")
			}
		}
		return multiplierBreakdown.map { it.miniMessage() }.reduceOrNull { acc, component -> acc.appendNewline().append(component) }
	}

	private fun formatAmount(providerKey: String, amount: Double): Component? {
		val (providerId, argument) = getProviderKey(providerKey)
		val multiplier = calculateMultiplier(providerKey)
		val provider = IncomeSellAPI.getProvider(providerId) ?: return null
		if (!provider.acceptsMultipliers(argument)) return provider.displayString(argument, amount).miniMessage()
		return provider.displayString(argument, amount * multiplier).miniMessage()
	}

	private fun formatTransactionMap(transactions: Map<String, Double>): Component? {
		if (transactions.isEmpty()) return null
		val mappedComponents = transactions.mapNotNull { formatAmount(it.key, it.value) }.toMutableList()
		if (mappedComponents.isEmpty()) return null
		return mappedComponents.reduceOrNull { acc, component -> acc.appendSpace().append(component) }
	}

	private fun formatItemTransaction(
		itemTransaction: ItemTransaction,
		format: String = "&8| &f<item_amount>x &7<item_name> &8→ &7<transaction_total>"
	): Component? {
		val transactionTotal = Placeholder.component("transaction_total", formatTransactionMap(itemTransaction.transactions) ?: return null)
		val itemName = Placeholder.component("item_name", itemTransaction.item.effectiveName())
		val itemAmount = Placeholder.unparsed("item_amount", INTEGER_NUMBER_FORMAT.format(itemTransaction.amountSold))
		return format.miniMessage(transactionTotal, itemName, itemAmount)
	}

	private fun getProviderKey(providerKey: String): Pair<String, String?> {
		val parts = providerKey.split("/", limit = 2)
		val provider = parts[0]
		val argument = parts.getOrNull(1)
		return Pair(provider, argument)
	}
	//endregion

}

