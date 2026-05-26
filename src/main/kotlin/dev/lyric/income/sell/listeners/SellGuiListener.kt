package dev.lyric.income.sell.listeners

import dev.lyric.income.sell.IncomeSell
import dev.lyric.income.sell.api.IncomeSellAPI
import dev.lyric.income.sell.sellgui.SellGui
import dev.lyric.income.sell.sellgui.SellGuiHolder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack

class SellGuiListener : Listener {

	private val plugin = IncomeSell.instance

	@EventHandler
	fun onInventoryClose(event: InventoryCloseEvent) {
		if (event.inventory.holder !is SellGuiHolder) return
		val player = event.player as? Player ?: return
		val items = mutableListOf<ItemStack>()
		for (slot in 0 until 36)
			items.add(event.inventory.getItem(slot) ?: continue)
		player.give(items)
		Bukkit.getScheduler().runTaskLater(plugin, Runnable {
			player.updateInventory()
		}, 1L)
	}

	@EventHandler
	fun onInventoryClick(event: InventoryClickEvent) {
		if (event.clickedInventory?.holder !is SellGuiHolder) return
		if (event.slot !in 36 until 45) return
		event.isCancelled = true
		val clickedItem = event.currentItem ?: return
		if (!clickedItem.persistentDataContainer.has(SellGui.sellActionKey)) return
		val sellResult = IncomeSellAPI.sellInventory(event.clickedInventory!!)
		if (sellResult.isEmpty()) return
		val player = event.whoClicked as? Player ?: return
		if (!sellResult.handlePayout(player)) return
		player.sendMessage(sellResult.getSoldMessage())
	}

}