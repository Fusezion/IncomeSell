package dev.lyric.income.sell.sellgui

import dev.lyric.income.sell.utils.AdventureUtils.component
import dev.lyric.income.sell.utils.AdventureUtils.miniMessage
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.datacomponent.item.TooltipDisplay
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType
import org.bukkit.persistence.PersistentDataType

@Suppress("UnstableApiUsage")
object SellGui {

	val sellActionKey = NamespacedKey.fromString("incomesell:sell_inventory")!!

	val borderItem: ItemStack by lazy {
		val itemStack = ItemType.BLACK_STAINED_GLASS_PANE.createItemStack()
		itemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build())
		return@lazy itemStack
	}

	val sellItem: ItemStack by lazy {
		val itemStack = ItemType.EMERALD.createItemStack()
		val lore = listOf<String>().map { it.miniMessage() }
		itemStack.setData(DataComponentTypes.CUSTOM_NAME, "&aClick to sell items".miniMessage())
		itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(lore))
		itemStack.editPersistentDataContainer { container -> container.set(sellActionKey, PersistentDataType.BOOLEAN, true) }
		return@lazy itemStack
	}

	fun createGui(player: Player): SellGuiHolder {
		val holder = SellGuiHolder()
		val inventory = Bukkit.createInventory(holder, 45, "Place in items to sell!".component())

		for (index in 36 until 45) inventory.setItem(index, borderItem)
		inventory.setItem(40, sellItem)
		holder.guiInventory = inventory
		return holder
	}

}