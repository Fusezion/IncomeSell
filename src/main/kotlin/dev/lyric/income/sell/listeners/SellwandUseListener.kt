package dev.lyric.income.sell.listeners

import dev.lyric.income.sell.IncomeSell
import dev.lyric.income.sell.api.IncomeSellAPI
import dev.lyric.income.sell.config.ConfigManager
import dev.lyric.income.sell.config.data.SellwandConfig
import dev.lyric.income.sell.messages.MessageTagResolvers
import dev.lyric.income.sell.messages.Messages
import dev.lyric.income.sell.utils.PDCKey
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.text.NumberFormat

class SellwandUseListener : Listener {

	val messages: Messages
		get() = IncomeSell.messages

	@EventHandler
	fun onRightClick(event: PlayerInteractEvent) {
		if (!validateEvent(event)) return
		val containerInventory = (event.clickedBlock?.state as Container).inventory

		val sellwandItem = event.item!!
		val player = event.player
		if (!isSellwand(sellwandItem)) return
		event.isCancelled = true
		if (containerInventory.isEmpty) return
		if (containerInventory.filterNotNull().all { !IncomeSellAPI.hasSellActions(it) }) return
		if (!applyCooldown(player, sellwandItem)) return

		val sellWandPDC = sellwandItem.persistentDataContainer.get(PDCKey.sellwand, PersistentDataType.TAG_CONTAINER)!!
		val type = sellWandPDC.get(PDCKey.type, PersistentDataType.STRING)!!
		if (!ConfigManager.isValidFolderConfigChild("sellwands/$type")) return
		val sellwandConfig = ConfigManager.getConfigFromFolder<SellwandConfig>("sellwands", type)

		val sellResult = IncomeSellAPI.createEmptySellResult()
		sellResult.recordInventoryTransaction(containerInventory)
		if (!sellResult.hasTransactions()) {
			player.sendActionBar(messages.resolve { bulkSell.nothingSold })
			return
		}
		sellResult.editGlobalMultiplier(sellwandConfig.multiplier)
		if (!IncomeSellAPI.payoutTransactions(player, sellResult)) return
		val resolvers = arrayOf(
			MessageTagResolvers.numberFormat("total_items", sellResult.getItemTransactions().sumOf { it.amountSold }),
			MessageTagResolvers.formatTotalCurrency(sellResult, "currency_breakdown", NumberFormat.Style.LONG),
			MessageTagResolvers.formatTotalCurrency(sellResult, "currency_breakdown_short", NumberFormat.Style.SHORT),
			MessageTagResolvers.breakdownTag(sellResult)
		)
		player.sendMessage { messages.resolve( { bulkSell.sellMessage }, *resolvers) }
		containerInventory.filterNotNull().filter(IncomeSellAPI::hasSellActions).forEach { it.amount = 0 }

		if (sellWandPDC.has(PDCKey.uses) && sellWandPDC.has(PDCKey.maxUses)) {
			val remainingUses = sellWandPDC.get(PDCKey.uses, PersistentDataType.INTEGER) ?: return
			val maxUses = sellWandPDC.get(PDCKey.maxUses, PersistentDataType.INTEGER) ?: return
			modifyUsages(sellwandItem, sellwandConfig, remainingUses - 1, maxUses)
		}


	}

	private fun isSellwand(item: ItemStack) = item.persistentDataContainer.has(PDCKey.sellwand)

	@Suppress("UnstableApiUsage")
	private fun applyCooldown(player: Player, item: ItemStack): Boolean {
		if (player.hasCooldown(item)) return false
		val useCooldown = item.getData(DataComponentTypes.USE_COOLDOWN) ?: return true
		player.setCooldown(item, (useCooldown.seconds() * 20).toInt().coerceIn(1, Int.MAX_VALUE))
		return true
	}

	@Suppress("UnstableApiUsage")
	private fun modifyUsages(item: ItemStack, sellwandConfig: SellwandConfig, remainingUses: Int, maxUses: Int) {
		if (remainingUses == 0) {
			item.amount = 0
			return
		}
		val tagResolvers = arrayOf<TagResolver>(
			Placeholder.unparsed("uses", remainingUses.toString()),
			Placeholder.unparsed("max_uses", maxUses.toString())
		)
		val damage = item.getData(DataComponentTypes.DAMAGE) ?: 0
		item.setData(DataComponentTypes.DAMAGE, damage + 1)
		item.setData(DataComponentTypes.LORE, ItemLore.lore(sellwandConfig.getLimitedUseLore(tagResolvers)))
		item.editPersistentDataContainer { pdc ->
			val sellwand = pdc.get(PDCKey.sellwand, PersistentDataType.TAG_CONTAINER)!!
			sellwand.set(PDCKey.uses, PersistentDataType.INTEGER, remainingUses)
			pdc.set(PDCKey.sellwand, PersistentDataType.TAG_CONTAINER, sellwand)
		}
	}

	private fun validateEvent(event: PlayerInteractEvent): Boolean {
		if (!event.hasItem() || !event.hasBlock()) return false
		if (event.clickedBlock?.state !is Container) return false
		return true
	}

}