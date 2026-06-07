package dev.lyric.income.sell.messages

import dev.lyric.income.sell.IncomeSell
import dev.lyric.income.sell.api.IncomeSellAPI
import dev.lyric.income.sell.api.IncomeSellAPI.getProviderAndArgument
import dev.lyric.income.sell.api.SellProviderRegistry
import dev.lyric.income.sell.api.SellResult
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.inventory.ItemStack
import java.text.NumberFormat

object MessageTagResolvers {

	/*
	<total_items>, <item_amount>, <multiplier>
	<currency_breakdown_short>, <currency_breakdown>
	<breakdown>
	<item_name>, <display_name>
	 */

	fun numberFormat(key: String, amount: Number) = Formatter.number(key, amount)

	fun stringFormat(key: String, string: String) = Placeholder.parsed(key, string)

	fun hoverableItem(item: ItemStack) =
		Placeholder.component("item_name", item.effectiveName().hoverEvent(item.asHoverEvent()))

	fun formatTotalCurrency(sellResult: SellResult, key: String, numberStyle: NumberFormat.Style): TagResolver {
		val mappedCurrencies = sellResult.getMergedTransactions().mapNotNull { (providerKey, amount) ->
			if (!IncomeSellAPI.isValidProvider(providerKey)) return@mapNotNull null
			val (providerId, argument) = getProviderAndArgument(providerKey)
			val provider = SellProviderRegistry.getProvider(providerId) ?: return@mapNotNull null
			val multiplier = sellResult.calculateMultiplier(providerKey)
			return@mapNotNull provider.formatAmount((amount * multiplier), argument, numberStyle)
		}.toList()
		return Placeholder.parsed(key, mappedCurrencies.reduce { acc, string -> "$acc $string" })
	}

	fun formatCurrencyMap(currencyMap: Map<String, Double>, key: String, numberStyle: NumberFormat.Style): TagResolver {
		val mappedCurrencies = currencyMap.mapNotNull { (providerKey, amount) ->
			if (!IncomeSellAPI.isValidProvider(providerKey)) return@mapNotNull null
			val (providerId, argument) = getProviderAndArgument(providerKey)
			val provider = SellProviderRegistry.getProvider(providerId) ?: return@mapNotNull null
			return@mapNotNull provider.formatAmount((amount), argument, numberStyle)
		}.toList()
		return Placeholder.parsed(key, mappedCurrencies.reduce { acc, string -> "$acc $string" })
	}


	val messages: Messages
		get() = IncomeSell.messages

	fun breakdownTag(sellResult: SellResult): TagResolver {
		var breakdownComponent = Component.empty()
		if (!sellResult.hasTransactions()) return Placeholder.component("breakdown", breakdownComponent)

		if (sellResult.hasMultipliers()) {
			breakdownComponent = breakdownComponent.append(messages.resolve { breakdown.multiplierHeader })
			if (sellResult.getGlobalMultiplier() != 1f) {
				breakdownComponent = breakdownComponent.appendNewline().append(
					messages.resolve(
						{ breakdown.globalMultiplier },
						numberFormat("multiplier", sellResult.getGlobalMultiplier())
					)
				)
			}
			val multiplierMap =
				getMultiplierMap(sellResult.getProviderMultipliers(), sellResult.getProviderSpecificMultipliers())
			if (multiplierMap.isNotEmpty()) {
				for ((providerId, argumentMap) in multiplierMap) {
					val provider = SellProviderRegistry.getProvider(providerId) ?: continue
					val providerDisplayName = stringFormat("display_name", provider.displayName)
					breakdownComponent = if (argumentMap.containsKey(null)) {
						breakdownComponent.appendNewline()
							.append(
								messages.resolve(
									{ breakdown.providerWithMultiplier },
									numberFormat("multiplier", argumentMap[null]!!),
									providerDisplayName
								)
							)
					} else {
						breakdownComponent.appendNewline()
							.append(messages.resolve({ breakdown.providerWithoutMultiplier }, providerDisplayName))
					}
					for ((argument, amount) in argumentMap) {
						if (argument == null) continue
						val argumentDisplayName = stringFormat("display_name", provider.argumentDisplayName(argument))
						val multiplier = numberFormat("multiplier", amount)
						breakdownComponent = breakdownComponent.appendNewline().append(
							messages.resolve({ breakdown.providerChildMultiplier }, argumentDisplayName, multiplier)
						)
					}
				}
			}
			if (sellResult.hasItemTransactions())
				breakdownComponent = breakdownComponent.appendNewline().appendNewline()
		}
		if (sellResult.hasItemTransactions()) {
			breakdownComponent = breakdownComponent.append(messages.resolve { breakdown.itemHeader })
			for (itemTransaction in sellResult.getItemTransactions()) {
				val resolvers = arrayOf(
					numberFormat("item_amount", itemTransaction.amountSold),
					hoverableItem(itemTransaction.item),
					formatCurrencyMap(itemTransaction.transactions, "currency_breakdown", NumberFormat.Style.LONG),
					formatCurrencyMap(
						itemTransaction.transactions,
						"currency_breakdown_short",
						NumberFormat.Style.SHORT
					)
				)
				breakdownComponent = breakdownComponent.appendNewline()
					.append(messages.resolve({ breakdown.itemFormat }, *resolvers))
			}
		}

		return Placeholder.styling("breakdown", HoverEvent.showText(breakdownComponent))

	}

	private fun getMultiplierMap(
		providerMap: Map<String, Float>,
		specificProviderMap: Map<String, Float>
	): Map<String, Map<String?, Float>> {
		val multiplierMap = mutableMapOf<String, MutableMap<String?, Float>>()
		for ((providerKey, amount) in providerMap) {
			val (providerId, _) = getProviderAndArgument(providerKey)
			multiplierMap.computeIfAbsent(providerId) { mutableMapOf() }[null] = amount
		}
		for ((providerKey, amount) in specificProviderMap) {
			val (providerId, argument) = getProviderAndArgument(providerKey)
			if (argument == null) continue
			multiplierMap.computeIfAbsent(providerId) { mutableMapOf() }[argument] = amount
		}
		return multiplierMap
	}

}