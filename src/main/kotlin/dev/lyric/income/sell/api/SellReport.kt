package dev.lyric.income.sell.api

import dev.lyric.income.economy.api.EconomyCollection
import dev.lyric.income.economy.api.EconomyEntry
import dev.lyric.income.sell.multiplier.Multiplier
import net.kyori.adventure.text.Component
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class SellReport {

	private val transactions = mutableListOf<EconomyEntry>()
	private val itemTransactions = mutableMapOf<Component, MutableList<ItemTransaction>>()
	private var multipliers = mutableListOf<Multiplier>()
	private var sellReportState: SellReportState = SellReportState.BUILDING

	fun record(economyEntry: EconomyEntry) {
		check(sellReportState == SellReportState.BUILDING) { "The sell report has already been marked as processed" }
		transactions.add(economyEntry)
	}

	fun record(economyCollection: EconomyCollection) {
		check(sellReportState == SellReportState.BUILDING) { "The sell report has already been marked as processed" }
		transactions.addAll(economyCollection)
	}

	fun recordInventory(inventory: Inventory) {
		check(sellReportState == SellReportState.BUILDING) { "The sell report has already been marked as processed" }
		inventory.storageContents
			.filterNotNull()
			.filter { IncomeSellAPI.hasSellData(it) }
			.forEach(::recordItemStack)
	}

	fun recordItemStack(item: ItemStack) {
		check(sellReportState == SellReportState.BUILDING) { "The sell report has already been marked as processed" }
		if (!IncomeSellAPI.hasSellData(item)) return
		val itemCopy = item.clone()
		val economyCollection = IncomeSellAPI.getSellData(itemCopy) ?: return
		val itemTransaction = ItemTransaction(itemCopy, itemCopy.amount, economyCollection)
		val effectiveName = itemCopy.effectiveName()
		itemTransactions.computeIfAbsent(effectiveName) { mutableListOf() }.add(itemTransaction)
		transactions.addAll(economyCollection)
	}

	fun getTransactions(): List<EconomyEntry> {
		return transactions.toList()
	}

	fun getItemTransactions(): Map<Component, List<ItemTransaction>> {
		return itemTransactions
	}

	fun recordMultiplier(multiplier: Multiplier) {
		check(sellReportState == SellReportState.BUILDING) { "The sell report has already been marked as processed" }
		multipliers += multiplier
	}

	fun isEmpty(): Boolean = transactions.isEmpty()

	fun getMultipliers(): List<Multiplier> {
		return multipliers.toList()
	}

	internal fun markCompleted() {
		this.sellReportState = SellReportState.PROCESSED
	}

	fun isCompleted() = this.sellReportState == SellReportState.PROCESSED

}
