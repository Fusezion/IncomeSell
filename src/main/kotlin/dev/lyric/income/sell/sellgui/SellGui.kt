package dev.lyric.income.sell.sellgui

import dev.lyric.income.sell.config.ConfigManager
import dev.lyric.income.sell.config.data.SellGuiConfig
import dev.lyric.income.sell.utils.AdventureUtils.component
import dev.lyric.income.sell.utils.AdventureUtils.miniMessage
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.datacomponent.item.ItemLore.lore
import io.papermc.paper.datacomponent.item.TooltipDisplay
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType
import org.bukkit.persistence.PersistentDataType
import kotlin.math.roundToInt

@Suppress("UnstableApiUsage")
object SellGui {

	val guiConfig: SellGuiConfig
		get() = ConfigManager.getConfig("sellgui")

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