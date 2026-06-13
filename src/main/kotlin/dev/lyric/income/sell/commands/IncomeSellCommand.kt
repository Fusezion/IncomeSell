package dev.lyric.income.sell.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import dev.lyric.config.ConfigManager
import dev.lyric.config.source.FolderConfigSource
import dev.lyric.income.sell.IncomeSell
import dev.lyric.income.sell.commands.argument.SellwandArgumentType
import dev.lyric.income.sell.config.SellwandConfig
import dev.lyric.income.sell.messages.MessageTagResolvers
import dev.lyric.income.sell.messages.Messages
import dev.lyric.income.sell.utils.PDCKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.text.NumberFormat

object IncomeSellCommand {

	val configManager: ConfigManager
		get() = IncomeSell.configManager
	val integerFormater: NumberFormat = NumberFormat.getIntegerInstance()
	val sellwandConfigSource: FolderConfigSource<SellwandConfig>
		get() = configManager.getFolderSource<SellwandConfig>("sellwands")!!
	var messages: Messages
		get() = IncomeSell.messages
		set(value) {
			IncomeSell.messages = value
		}

	fun createCommand(): LiteralCommandNode<CommandSourceStack> {
		return Commands.literal("incomesell")
			.requires { (it.executor ?: it.sender).hasPermission("incomesell.command.admin") }
			.then(
				Commands.literal("reload")
					.executes { context ->
						val source = context.source
						val executor = (source.executor ?: source.executor) ?: return@executes Command.SINGLE_SUCCESS
						configManager.loadAll()
						messages = Messages(configManager.getFile("messages")!!)
						executor.sendMessage { messages.resolve { command.reload } }
						return@executes Command.SINGLE_SUCCESS
					}
			)
			.then(
				Commands.literal("sellwand")
					.then(
						Commands.argument("player", ArgumentTypes.player())
							.then(
								Commands.argument("type", SellwandArgumentType())
									.suggests { _, builder ->
										for (fileKey in sellwandConfigSource.getKeys()) {
											builder.suggest(fileKey)
										}
										return@suggests builder.buildFuture()
									}
									.then(
										Commands.argument("uses", IntegerArgumentType.integer(1))
											.executes { context ->
												val source = context.source
												val executor = (source.executor ?: source.sender)
												val (player, sellwand, uses) =
													getSellwandArguments(executor, context, true)
												if (player == null || uses == null)
													return@executes Command.SINGLE_SUCCESS
												val resolvers = arrayOf(
													MessageTagResolvers.stringFormat("sellwand", sellwand.identifier),
													MessageTagResolvers.numberFormat("uses", uses)
												)
												player.sendMessage {
													messages.resolve(
														{ command.playerReceivedSellwandWithUses },
														*resolvers
													)
												}
												giveSellwandCommand(player, sellwand, uses)
												return@executes Command.SINGLE_SUCCESS
											}
									)
									.executes { context ->
										val source = context.source
										val executor = (source.executor ?: source.sender)
										val (player, sellwand, _) = getSellwandArguments(executor, context, false)
										if (player == null) return@executes Command.SINGLE_SUCCESS
										giveSellwandCommand(player, sellwand, null)
										player.sendMessage {
											messages.resolve(
												{ command.playerReceivedSellwand },
												MessageTagResolvers.stringFormat("sellwand", sellwand.identifier)
											)
										}
										return@executes Command.SINGLE_SUCCESS
									}
							)
					)
			)
			.build()
	}

	private fun getSellwandArguments(
		executor: CommandSender,
		context: CommandContext<CommandSourceStack>,
		hasUses: Boolean
	): Triple<Player?, SellwandConfig, Int?> {
		val playerSelector = context.getArgument("player", PlayerSelectorArgumentResolver::class.java)
		val player = playerSelector.resolve(context.source).firstOrNull()
		if (player == null) executor.sendMessage { messages.resolve { command.noPlayerFound } }
		val sellwand = context.getArgument("type", SellwandConfig::class.java)
		if (hasUses) {
			val uses = IntegerArgumentType.getInteger(context, "uses")
			return Triple(player, sellwand, uses)
		}
		return Triple(player, sellwand, null)
	}

	private fun giveSellwandCommand(player: Player, sellwand: SellwandConfig, uses: Int?) {
		val sellwandItem = if (uses == null) {
			sellwand.getUnlimitedUsesItem().also { itemStack ->
				itemStack.editPersistentDataContainer {
					val sellwandPDC = it.adapterContext.newPersistentDataContainer()
					sellwandPDC.set(PDCKey.type, PersistentDataType.STRING, sellwand.identifier)
					it.set(PDCKey.sellwand, PersistentDataType.TAG_CONTAINER, sellwandPDC)
				}
			}
		} else {
			createLimitedUseSellwand(uses, sellwand)
		}
		player.give(sellwandItem)
	}

	@Suppress("UnstableApiUsage")
	private fun createLimitedUseSellwand(uses: Int, sellwand: SellwandConfig): ItemStack {
		val resolvers = arrayOf<TagResolver>(
			Placeholder.unparsed("uses", integerFormater.format(uses)),
			Placeholder.unparsed("max_uses", integerFormater.format(uses))
		)
		val sellwandItem = sellwand.getLimitedUsesItem(resolvers)
		sellwandItem.setData(DataComponentTypes.MAX_DAMAGE, uses)
		sellwandItem.setData(DataComponentTypes.DAMAGE, 0)
		sellwandItem.editPersistentDataContainer { pdc ->
			val subContainer = pdc.adapterContext.newPersistentDataContainer()
			subContainer.set(PDCKey.uses, PersistentDataType.INTEGER, uses)
			subContainer.set(PDCKey.maxUses, PersistentDataType.INTEGER, uses)
			subContainer.set(PDCKey.type, PersistentDataType.STRING, sellwand.identifier)
			pdc.set(PDCKey.sellwand, PersistentDataType.TAG_CONTAINER, subContainer)
		}
		return sellwandItem
	}

}