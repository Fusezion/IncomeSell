package dev.lyric.income.sell.utils

import dev.lyric.income.economy.api.EconomyCollection
import dev.lyric.income.sell.api.IncomeSellAPI
import dev.lyric.income.sell.api.ItemTransaction
import dev.lyric.income.sell.api.SellReport
import dev.lyric.income.sell.multiplier.DisplayableMultiplier
import dev.lyric.income.sell.multiplier.Multiplier
import dev.lyric.income.sell.multiplier.default.DisplayableProviderMultiplier
import dev.lyric.income.sell.multiplier.default.DisplayableSimpleMultiplier
import dev.lyric.income.sell.utils.AdventureUtils.minimessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object SellUtils {

	private val MUST_HOLD_AN_ITEM = " &8• &cYour must be holding an item to sell".minimessage()
	private val HELD_ITEM_CANNOT_BE_SOLD = " &8• &cYour held item cannot be sold".minimessage()
	private val EMPTY_INVENTORY = " &8• &cThere is nothing in the inventory to sell".minimessage()
	private val SOLD_ITEM: (Array<TagResolver>) -> Component = { resolvers ->
		" &8• &aSold <amount>x <name> for <total> <breakdown>".minimessage(*resolvers)
	}
	private val SOLD_INVENTORY: (Array<TagResolver>) -> Component = { resolvers ->
		" &8• &aSold <amount> items for <total> <breakdown>".minimessage(*resolvers)
	}
	private val SOLD_INVENTORY_NO_BREAKDOWN: (Array<TagResolver>) -> Component = { resolvers ->
		" &8• &aSold <amount> items for <total>".minimessage(*resolvers)
	}
	private val ITEM_BREAKDOWN_FORMAT: (Array<TagResolver>) -> Component = { resolvers ->
		"  &8➥ &f<amount>x <display_name>&7: <total>".minimessage(* resolvers)
	}
	private val MULTIPLIER_BREAKDOWN_FORMAT: (Array<TagResolver>) -> Component = { resolvers ->
		"  &8➥ &f<display_name>&7: <amount>x".minimessage(* resolvers)
	}

	fun processItemSell(player: Player, item: ItemStack, multipliers: List<Multiplier> = emptyList()): Boolean {
		if (item.isEmpty) {
			player.sendMessage(MUST_HOLD_AN_ITEM)
			return false
		}
		if (!IncomeSellAPI.hasSellData(item)) {
			player.sendMessage(HELD_ITEM_CANNOT_BE_SOLD)
			return false
		}
		// Sell Report Recording
		val sellReport = IncomeSellAPI.createSellReport()
		sellReport.recordItemStack(item)
		if (sellReport.isEmpty()) {
			player.sendMessage(HELD_ITEM_CANNOT_BE_SOLD)
			return false
		}
		multipliers.forEach(sellReport::recordMultiplier)
		// Sell Report Transaction Handler
		if (IncomeSellAPI.processSell(player, sellReport)) {
			val transactionTotal = EconomyCollection(sellReport.getTransactions()).format()
			val resolvers = arrayOf(
				Formatter.number("amount", item.amount),
				Placeholder.component("name", item.effectiveName()),
				Placeholder.component("total", transactionTotal.minimessage()),
				Placeholder.component("breakdown", formatSellBreakdown(sellReport))
			)
			player.sendMessage(SOLD_ITEM.invoke(resolvers))
			return true
		}
		return false
	}

	fun processItemListSell(player: Player, items: List<ItemStack>, multipliers: List<Multiplier> = emptyList()): Boolean {
		if (items.isEmpty()) return false
		val sellReport = IncomeSellAPI.createSellReport()
		items.forEach(sellReport::recordItemStack)
		if (sellReport.isEmpty()) return false
		multipliers.forEach(sellReport::recordMultiplier)
		return handleBreakdownSell(player, sellReport)
	}

	fun processInventorySell(player: Player, inventory: Inventory, multipliers: List<Multiplier> = emptyList()): Boolean {
		if (inventory.isEmpty) {
			player.sendActionBar(EMPTY_INVENTORY)
			return false
		}
		if (inventory.filterNotNull().none { IncomeSellAPI.hasSellData(it) }) {
			player.sendActionBar(EMPTY_INVENTORY)
			return false
		}
		val sellReport = IncomeSellAPI.createSellReport()
		sellReport.recordInventory(inventory)
		if (sellReport.isEmpty()) {
			player.sendActionBar(EMPTY_INVENTORY)
			return false
		}
		multipliers.forEach(sellReport::recordMultiplier)
		return handleBreakdownSell(player, sellReport)
	}

	fun formatSellBreakdown(sellReport: SellReport): Component {
		val displayedMultipliers = sellReport.getMultipliers().filterIsInstance<DisplayableMultiplier>()
		val itemTransactions = sellReport.getItemTransactions()
		var breakdownComponent = Component.empty()
		if (displayedMultipliers.isEmpty() && itemTransactions.isEmpty())
			return breakdownComponent
		if (displayedMultipliers.isNotEmpty()) {
			val simpleMultipliers = displayedMultipliers.filterIsInstance<DisplayableSimpleMultiplier>()
			val providerMultipliers = displayedMultipliers.filterIsInstance<DisplayableProviderMultiplier>()
			val otherMultipliers = displayedMultipliers.filterNot { providerMultipliers.contains(it) || simpleMultipliers.contains(it) }

			breakdownComponent = breakdownComponent.append { "&8➥ &7Multipliers".minimessage() }
			if (simpleMultipliers.isNotEmpty()) {
				breakdownComponent = breakdownComponent.appendNewline().append(formatMultipliers(simpleMultipliers))
			}
			if (providerMultipliers.isNotEmpty()) {
				breakdownComponent = breakdownComponent.appendNewline().append(formatMultipliers(providerMultipliers))
			}
			if (otherMultipliers.isNotEmpty()) {
				breakdownComponent = breakdownComponent.appendNewline().append(formatMultipliers(otherMultipliers))
			}
			if (itemTransactions.isNotEmpty())
				breakdownComponent = breakdownComponent.appendNewline().appendNewline()
		}
		if (itemTransactions.isNotEmpty()) {
			breakdownComponent = breakdownComponent
				.append { "&8➥ &7Item Transactions".minimessage() }
				.appendNewline()
				.append(formatItemTransactions(itemTransactions))
		}
		return "&7[HOVER]".minimessage().hoverEvent(breakdownComponent.asHoverEvent())
	}

	private fun formatItemTransactions(itemTransactions: Map<Component, List<ItemTransaction>>): Component {
		val formattedTransactions = itemTransactions.map { (effectiveName, transactions) ->
			val total = EconomyCollection(transactions.flatMap { it.transactions }).format().minimessage()
			val itemAmount = transactions.sumOf { it.itemAmount }
			val resolvers = arrayOf(
				Placeholder.component("display_name", effectiveName),
				Placeholder.component("total", total),
				Formatter.number("amount", itemAmount),
			)
			return@map ITEM_BREAKDOWN_FORMAT.invoke(resolvers)
		}
		return formattedTransactions.reduce { acc, component -> acc.appendNewline().append(component) }
	}

	private fun formatMultipliers(multipliers: List<DisplayableMultiplier>): Component {
		val formattedMultipliers = multipliers.map {
			val resolvers = arrayOf(
				Placeholder.component("display_name", it.displayName.minimessage()),
				Formatter.number("amount", it.multiplier)
			)
			return@map MULTIPLIER_BREAKDOWN_FORMAT.invoke(resolvers)
		}
		return formattedMultipliers.reduce { acc, component -> acc.appendNewline().append(component) }
	}

	private fun handleBreakdownSell(player: Player, sellReport: SellReport): Boolean {
		if (IncomeSellAPI.processSell(player, sellReport)) {
			val transactionTotal = EconomyCollection(sellReport.getTransactions()).format()
			val totalItemsSold = sellReport.getItemTransactions().values.flatten().sumOf { it.itemAmount }
			val resolvers = arrayOf(
				Placeholder.component("total", transactionTotal.minimessage()),
				Formatter.number("amount", totalItemsSold),
				Placeholder.component("breakdown", formatSellBreakdown(sellReport))
			)
			player.sendMessage(SOLD_INVENTORY.invoke(resolvers))
			player.sendActionBar(SOLD_INVENTORY_NO_BREAKDOWN.invoke(resolvers))
			return true
		}
		return false
	}

}