package dev.lyric.income.sell.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import dev.lyric.config.ConfigManager
import dev.lyric.income.sell.IncomeSellPlugin
import dev.lyric.income.sell.api.IncomeSellAPI
import dev.lyric.income.sell.config.SellGuiConfig
import dev.lyric.income.sell.utils.AdventureUtils.minimessage
import dev.lyric.income.sell.utils.SellUtils
import dev.triumphteam.gui.components.GuiAction
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import dev.triumphteam.gui.guis.StorageGui
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

object SellGuiCommand {

	private val plugin: IncomeSellPlugin
		get() = IncomeSellPlugin.instance

	private val configManager: ConfigManager
		get() = IncomeSellPlugin.configManager

	private val sellGui: SellGuiConfig?
		get() = configManager.getFile("sellgui")

	fun registerCommand() {
		CommandAPICommand("sellgui")
			.executesPlayer(PlayerCommandExecutor { player, _ ->
				if (sellGui == null) {
					player.sendMessage { " &8• &cCould not load the sell gui config.".minimessage() }
					return@PlayerCommandExecutor
				}
				createSellGUI().open(player)
			})
			.register()
	}

	private fun createSellGUI(): StorageGui {
		val sellGui = sellGui ?: throw IllegalArgumentException("SellGui config was not found")
		val layoutString = sellGui.layout.joinToString(separator = "")
		val sellableSlots = layoutString.mapIndexedNotNull { index, char -> index.takeIf { char == sellGui.sellActionSlots.sellableSlot } }
		val storageGui = Gui.storage()
			.rows(sellGui.layout.size)
			.title(sellGui.title.minimessage())
			.create()
		for ((slotChar, itemBuilder) in sellGui.items) {
			val guiItem = GuiItem(itemBuilder.createItemStack())
			val slots = layoutString.mapIndexedNotNull { index, ch -> index.takeIf { ch == slotChar } }
			if (slotChar == sellGui.sellActionSlots.sellAction) {
				addSellGuiAction(guiItem, sellableSlots)
			} else {
				guiItem.action = { event -> event.isCancelled = true }
			}
			storageGui.setItem(slots, guiItem)
		}
		storageGui.setCloseGuiAction { event ->
			val player = event.player as? Player ?: return@setCloseGuiAction
			val returnedItems = event.inventory
				.filterIndexed { index, _ -> sellableSlots.contains(index) }
				.filterNotNull()
			player.give(returnedItems)
			Bukkit.getScheduler().runTask(plugin, Runnable { player.updateInventory() })
		}
		return storageGui
	}

	private fun addSellGuiAction(guiItem: GuiItem, sellableSlots: List<Int>) {
		guiItem.action = GuiAction { event ->
			event.isCancelled = true
			if (event.click != ClickType.LEFT) return@GuiAction
			val player = event.whoClicked as? Player ?: return@GuiAction

			val sellableItems = event.inventory.storageContents
				.mapIndexedNotNull { index, itemStack -> itemStack.takeIf { sellableSlots.contains(index) } }
				.filter { IncomeSellAPI.hasSellData(it) }
			if (SellUtils.processItemListSell(player, sellableItems)) {
				sellableItems.forEach { it.amount = 0 }
			}
		}
	}

}