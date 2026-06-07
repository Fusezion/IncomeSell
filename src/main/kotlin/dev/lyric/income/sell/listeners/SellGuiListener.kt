package dev.lyric.income.sell.listeners

import dev.lyric.income.sell.IncomeSell
import dev.lyric.income.sell.api.IncomeSellAPI
import dev.lyric.income.sell.messages.MessageTagResolvers
import dev.lyric.income.sell.messages.Messages
import dev.lyric.income.sell.sellgui.SellGui
import dev.lyric.income.sell.sellgui.SellGuiHolder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import java.text.NumberFormat

class SellGuiListener : Listener {

	private val plugin = IncomeSell.instance
	val messages: Messages
		get() = IncomeSell.messages

	@EventHandler
	fun onInventoryClose(event: InventoryCloseEvent) {
		if (event.inventory.holder !is SellGuiHolder) return
		val player = event.player as? Player ?: return
		val items = mutableListOf<ItemStack>()
		for (slot in 0 until 45)
			items.add(event.inventory.getItem(slot) ?: continue)
		player.give(items)
		Bukkit.getScheduler().runTaskLater(plugin, Runnable {
			player.updateInventory()
		}, 1L)
	}

	@EventHandler
	fun onInventoryClick(event: InventoryClickEvent) {
		if (event.clickedInventory?.holder !is SellGuiHolder) return
		val clickedItem = event.currentItem ?: return
		if (!clickedItem.persistentDataContainer.has(SellGui.sellGuiItem)) return
		event.isCancelled = true
		if (!clickedItem.persistentDataContainer.has(SellGui.sellActionKey)) return
		val sellResult = IncomeSellAPI.createEmptySellResult()
		sellResult.recordInventoryTransaction(event.clickedInventory!!)
		if (!sellResult.hasTransactions()) return
		val player = event.whoClicked as? Player ?: return
		if (!IncomeSellAPI.payoutTransactions(player, sellResult)) return

		val totalItemsSold = sellResult.getItemTransactions().sumOf { it.amountSold }
		val resolvers = arrayOf(
			MessageTagResolvers.numberFormat("total_items", totalItemsSold),
			MessageTagResolvers.formatTotalCurrency(sellResult, "currency_breakdown", NumberFormat.Style.LONG),
			MessageTagResolvers.formatTotalCurrency(sellResult, "currency_breakdown_short", NumberFormat.Style.SHORT),
			MessageTagResolvers.breakdownTag(sellResult)
		)
		player.sendMessage(messages.resolve({ bulkSell.sellMessage }, *resolvers))
		event.inventory.filterNotNull().filter(IncomeSellAPI::hasSellActions).forEach { it.amount = 0 }
	}

}