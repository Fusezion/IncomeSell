package dev.lyric.income.sell.listeners

import dev.lyric.income.sell.api.IncomeSellAPI
import dev.lyric.income.sell.config.ConfigManager
import dev.lyric.income.sell.config.data.SellwandConfig
import dev.lyric.income.sell.utils.AdventureUtils.component
import dev.lyric.income.sell.utils.PDCKey
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class SellwandUseListener : Listener {

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
		val multiplier = sellwandConfig.multiplier

		val sellResult = IncomeSellAPI.sellInventory(containerInventory)
		if (sellResult.isEmpty()) {
			player.sendActionBar("There was nothing in the container to sell".component(NamedTextColor.RED))
			return
		}
		sellResult.editGlobalMultiplier(multiplier)
		if (!sellResult.handlePayout(player)) return
		player.sendMessage(sellResult.getSoldMessage())

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