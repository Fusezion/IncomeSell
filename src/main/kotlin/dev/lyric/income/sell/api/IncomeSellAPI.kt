package dev.lyric.income.sell.api

import dev.lyric.income.sell.api.events.PlayerSellEvent
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object IncomeSellAPI {

	private val SELL_ACTION_KEY = NamespacedKey.fromString("incomesell:sell_actions")!!
	private val PROVIDER_KEY_REGEX = Regex("[a-z0-9._-]+")

	/**
	 * Creates an empty unmodified sell result perfect for usage outside of selling items.
	 */
	@JvmStatic
	fun createEmptySellResult(): SellResult = SellResult()

	/**
	 * Checks whether the provided [item] has the required information about sell actions attached.
	 */
	@JvmStatic
	fun hasSellActions(item: ItemStack): Boolean {
		return item.persistentDataContainer.has(SELL_ACTION_KEY, PersistentDataType.TAG_CONTAINER)
				&& getSellActions(item).isNotEmpty()
	}

	/**
	 * Adds a sell action to the persistent data container of the [item].
	 *
	 * @throws IllegalArgumentException if [item] is empty see [ItemStack.isEmpty]
	 * @throws IllegalArgumentException if [amount] is less than or equal to 0
	 * @throws IllegalArgumentException if [providerId] does not match the regex `[a-z0-9._-]`
	 * @throws IllegalArgumentException if [argument] is not null and does not match the regex `[a-z0-9._-]`
	 */
	@JvmStatic
	fun addSellAction(item: ItemStack, providerId: String, amount: Double, argument: String? = null, overwrite: Boolean = false): ItemStack {
		require(!item.isEmpty) { "Provided itemstack cannot be empty" }
		require(amount > 0) { "Amount must not be less than or equal to 0" }
		require(providerId.matches(PROVIDER_KEY_REGEX)) { "provider id does not match the regex [a-z0-9._-]" }
		if (argument != null) require(argument.matches(PROVIDER_KEY_REGEX)) { "argument does not match the regex [a-z0-9._-]" }

		val sellActions = getSellActions(item)
		val providerId = providerId.lowercase()
		val argument = argument?.lowercase()
		val key = if (argument.isNullOrBlank()) providerId else "${providerId}/${argument}"
		sellActions[key] = if (overwrite) amount else amount.plus(sellActions.getOrDefault(key, 0.0))
		setSellActions(item, sellActions)
		return item
	}

	/**
	 * Gets the sell-actions of the provided [item]
	 */
	@JvmStatic
	fun getSellActions(item: ItemStack): MutableMap<String, Double> {
		val sellActions = mutableMapOf<String, Double>()
		val dataContainer = item.persistentDataContainer
		val sellActionsContainer = dataContainer.get(SELL_ACTION_KEY, PersistentDataType.TAG_CONTAINER) ?: return sellActions
		for (key in sellActionsContainer.keys) {
			if (!sellActionsContainer.has(key, PersistentDataType.DOUBLE)) continue
			val amount = sellActionsContainer.get(key, PersistentDataType.DOUBLE) ?: continue
			sellActions[key.key] = amount
		}
		return sellActions
	}

	/**
	 * Overwrites the sell-actions of the provided [item] to everything inside the [sellActions] map
	 */
	@JvmStatic
	fun setSellActions(item: ItemStack, sellActions: Map<String, Double>) {
		item.editPersistentDataContainer { persistentDataContainer ->
			val sellActionPDC = persistentDataContainer.adapterContext.newPersistentDataContainer()
			for ((sellKey, amount) in sellActions) {
				val sellActionKey = NamespacedKey.fromString("incomesell:$sellKey") ?: continue
				sellActionPDC.set(sellActionKey, PersistentDataType.DOUBLE, amount)
			}
			persistentDataContainer.set(SELL_ACTION_KEY, PersistentDataType.TAG_CONTAINER, sellActionPDC)
		}
	}

	@JvmStatic
	fun payoutTransactions(player: Player, sellResult: SellResult): Boolean {
		val wasCancelled = !PlayerSellEvent(player, sellResult).callEvent()
		if (wasCancelled) return false
		val validTransactions = sellResult.getTransactions().filter { isValidProvider(it.key) }.toMutableMap()
		val itemTransactions = sellResult.getItemTransactions()
		for (itemTransaction in itemTransactions) {
			for ((providerKey, amount) in itemTransaction.transactions) {
				if (!isValidProvider(providerKey)) continue
				validTransactions.merge(providerKey, amount, Double::plus)
			}
		}
		if (validTransactions.isEmpty()) return false
		var paidOutAnything = false
		for ((providerKey, amount) in validTransactions) {
			if (!isValidProvider(providerKey)) continue
			val (providerId, argument) = getProviderAndArgument(providerKey)
			val provider = SellProviderRegistry.getProvider(providerId) ?: continue
			val multiplier = sellResult.calculateMultiplier(providerKey)
			val finalAmount = amount * multiplier
			provider.handleSell(player, argument, finalAmount)
			if (!paidOutAnything) paidOutAnything = true
		}
		return paidOutAnything
	}

	@JvmStatic
	internal fun getProviderAndArgument(providerKey: String): Pair<String, String?> {
		val parts = providerKey.split("/", limit = 2)
		val provider = parts[0]
		val argument = parts.getOrNull(1)
		return Pair(provider, argument)
	}

	@JvmStatic
	internal fun isValidProvider(providerKey: String): Boolean {
		val (providerId, argument) = getProviderAndArgument(providerKey)
		val provider = SellProviderRegistry.getProvider(providerId) ?: return false
		return argument == null || provider.isValidArgument(argument)
	}

}