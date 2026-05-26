@file:Suppress("UnstableApiUsage")

package dev.lyric.income.sell.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.EntitySelectorArgument
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.LiteralArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.optionalArgument
import dev.lyric.income.sell.config.ConfigManager
import dev.lyric.income.sell.config.data.SellwandConfig
import dev.lyric.income.sell.utils.AdventureUtils.component
import dev.lyric.income.sell.utils.PDCKey
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.text.NumberFormat
import java.util.concurrent.CompletableFuture

object SellwandCommand {

	val integerFormater: NumberFormat = NumberFormat.getIntegerInstance()

	fun register() {
		CommandAPICommand("sellwand")
			.withPermission("incomesell.command.sellwand")
			.argument(LiteralArgument("give"))
			.argument(EntitySelectorArgument.OnePlayer("player"))
			.argument(StringArgument("sellwand").replaceSuggestions(ArgumentSuggestions.stringsAsync {
				CompletableFuture.supplyAsync { ConfigManager.getFolderConfig<SellwandConfig>("sellwands").getFileKeys().toTypedArray() }
			}))
			.argument(IntegerArgument("uses").setOptional(true).replaceSuggestions(ArgumentSuggestions.strings("100", "1000", "250")))
			.executesPlayer(PlayerCommandExecutor { sourcePlayer, arguments ->
				val receiver = arguments.get("player") as? Player ?: return@PlayerCommandExecutor
				val sellwand = arguments.get("sellwand") as? String ?: return@PlayerCommandExecutor
				val uses = arguments.get("uses") as? Int
				if (!ConfigManager.isValidFolderConfigChild("sellwands/$sellwand")) {
					sourcePlayer.sendMessage { "Invalid/Unknown sellwand provided: $sellwand".component(NamedTextColor.RED) }
					return@PlayerCommandExecutor
				}
				val sellwandConfig = ConfigManager.getConfigFromFolder<SellwandConfig>("sellwands", sellwand)
				if (uses == null) {
					val sellwandItem = sellwandConfig.getUnlimitedUsesItem()
					sellwandItem.editPersistentDataContainer {
						val sellwandPDC = it.adapterContext.newPersistentDataContainer()
						sellwandPDC.set(PDCKey.type, PersistentDataType.STRING, sellwand)
						it.set(PDCKey.sellwand, PersistentDataType.TAG_CONTAINER, sellwandPDC)
					}
					receiver.give(sellwandItem)
					return@PlayerCommandExecutor
				}
				val sellwandItem = createLimitedUseSellwand(uses, sellwand, sellwandConfig)
				receiver.give(sellwandItem)
			})
			.register()
	}

	private fun createLimitedUseSellwand(uses: Int, sellwand: String, sellwandConfig: SellwandConfig): ItemStack {
		val resolvers = arrayOf<TagResolver>(
			Placeholder.unparsed("uses", integerFormater.format(uses)),
			Placeholder.unparsed("max_uses", integerFormater.format(uses))
		)
		val sellwandItem = sellwandConfig.getLimitedUsesItem(resolvers)
		sellwandItem.setData(DataComponentTypes.MAX_DAMAGE, uses)
		sellwandItem.setData(DataComponentTypes.DAMAGE, 0)
		sellwandItem.editPersistentDataContainer { pdc ->
			val subContainer = pdc.adapterContext.newPersistentDataContainer()
			subContainer.set(PDCKey.uses, PersistentDataType.INTEGER, uses)
			subContainer.set(PDCKey.maxUses, PersistentDataType.INTEGER, uses)
			subContainer.set(PDCKey.type, PersistentDataType.STRING, sellwand)
			pdc.set(PDCKey.sellwand, PersistentDataType.TAG_CONTAINER, subContainer)
		}
		return sellwandItem
	}

}