package dev.lyric.income.sell.sellgui

import dev.lyric.income.sell.IncomeSell
import dev.lyric.income.sell.config.SellGuiConfig
import dev.lyric.income.sell.utils.AdventureUtils.miniMessage
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType

object SellGui {

	val guiConfig: SellGuiConfig
		get() = IncomeSell.configManager.getFile("sellgui")!!

	val sellActionKey = NamespacedKey.fromString("incomesell:sell_inventory")!!
	val sellGuiItem = NamespacedKey.fromString("incomesell:sell_gui_item")!!

	fun createGui(): SellGuiHolder {
		val holder = SellGuiHolder()
		val inventory = Bukkit.createInventory(holder, 54, guiConfig.inventoryName.miniMessage())
		val slotMap = getSlotMap(guiConfig.shape)
		if (slotMap.isNotEmpty()) {
			for (slotChar in slotMap.keys) {
				val item = (guiConfig.items[slotChar] ?: continue).createItemStack(emptyArray())
				item.editPersistentDataContainer { container ->
					container.set(sellGuiItem, PersistentDataType.BOOLEAN, true)
					if (slotChar == guiConfig.sellActionSlot) {
						container.set(sellActionKey, PersistentDataType.BOOLEAN, true)
					}
				}
				slotMap[slotChar]?.forEach { inventory.setItem(it, item) }
			}
		}
		holder.guiInventory = inventory
		return holder
	}

	private fun getSlotMap(shape: List<String>): Map<Char, List<Int>> {
		var index = 0
		val slotMap = mutableMapOf<Char, MutableList<Int>>()
		for (row in shape) {
			for (char in row) {
				slotMap.computeIfAbsent(char) { mutableListOf() }.add(index)
				index++
			}
		}
		return slotMap
	}

}