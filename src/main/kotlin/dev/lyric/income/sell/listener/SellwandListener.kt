package dev.lyric.income.sell.listener

import dev.lyric.config.ConfigManager
import dev.lyric.income.sell.IncomeSellPlugin
import dev.lyric.income.sell.api.IncomeSellAPI
import dev.lyric.income.sell.config.SellwandConfig
import dev.lyric.income.sell.multiplier.default.DisplayableSimpleMultiplier
import dev.lyric.income.sell.utils.AdventureUtils.minimessage
import dev.lyric.income.sell.utils.PDCKeys
import dev.lyric.income.sell.utils.SellUtils
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class SellwandListener: Listener {

	companion object {
		private val INVALID_SELLWAND_ITEM = " &8• &cYour tool is marked as sellwand but cannot get the type".minimessage()
		private val INVALID_SELLWAND_DATA = " &8• &cCould not retrieve the sellwand data for this item".minimessage()
		private val configManager: ConfigManager
			get() = IncomeSellPlugin.configManager
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	fun onBlockInteract(event: PlayerInteractEvent) {
		if (!event.hasBlock() || !event.hasItem()) return
		if (event.action != Action.RIGHT_CLICK_BLOCK) return
		if (!isSellwand(event.item!!)) return
		val containerInventory = getContainerInventory(event.clickedBlock!!) ?: return
		event.isCancelled = true
		val player = event.player

		val sellwandItem = event.item!!
		if (player.hasCooldown(sellwandItem)) return
		val sellwandPDC = sellwandItem.persistentDataContainer.get(PDCKeys.sellwand, PersistentDataType.TAG_CONTAINER) ?: return
		val sellwandType = sellwandPDC.get(PDCKeys.sellwandType, PersistentDataType.STRING)
		val sellwandUses = sellwandPDC.get(PDCKeys.sellwandUses, PersistentDataType.INTEGER)
		val sellwandMaxUses = sellwandPDC.get(PDCKeys.sellwandMaxUses, PersistentDataType.INTEGER)
		if (sellwandType == null) {
			player.sendMessage(INVALID_SELLWAND_ITEM)
			return
		}
		val sellwandConfig = configManager.getFolder<SellwandConfig>("sellwands", sellwandType)
		if (sellwandConfig == null) {
			player.sendMessage(INVALID_SELLWAND_DATA)
			return
		}
		if (SellUtils.processInventorySell(player, containerInventory, listOf(DisplayableSimpleMultiplier(sellwandConfig.multiplier, "Sellwand")))) {
			containerInventory.filterNotNull().filter { IncomeSellAPI.hasSellData(it) }.forEach { it.amount = 0 }
			if (sellwandUses != null && sellwandMaxUses != null) {
				modifySellwand(player, sellwandItem, sellwandConfig, sellwandUses - 1, sellwandMaxUses)
			}
			player.setCooldown(sellwandItem, (sellwandConfig.cooldown * 20).toInt())
		}
	}

	private fun modifySellwand(player: Player, sellwandItem: ItemStack, sellwandConfig: SellwandConfig, uses: Int, maxUses: Int) {
		var replacementItem = sellwandConfig.createLimitedUseItem(uses, maxUses)
		if (sellwandItem.amount > 1 && uses <= 0) {
			player.broadcastSlotBreak(EquipmentSlot.HAND)
			replacementItem = sellwandConfig.createLimitedUseItem(maxUses, maxUses)
			sellwandItem.amount -= 1
		} else if (uses <= 0) {
			player.broadcastSlotBreak(EquipmentSlot.HAND)
			sellwandItem.amount = 0
			return
		}
		@Suppress("UnstableApiUsage")
		sellwandItem.copyDataFrom(replacementItem) { true }
	}

	private fun isSellwand(item: ItemStack): Boolean = item.persistentDataContainer.has(PDCKeys.sellwand)

	private fun getContainerInventory(block: Block): Inventory? = (block.state as? Container)?.inventory

}