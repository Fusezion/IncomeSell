package dev.lyric.income.sell.sellgui

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class SellGuiHolder : InventoryHolder {
	lateinit var guiInventory: Inventory
	override fun getInventory(): Inventory = guiInventory
}
