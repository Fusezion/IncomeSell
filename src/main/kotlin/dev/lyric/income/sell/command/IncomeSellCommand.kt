package dev.lyric.income.sell.command

import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.CustomArgument.MessageBuilder
import dev.jorel.commandapi.arguments.EntitySelectorArgument
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.LiteralArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.executors.ExecutorType
import dev.lyric.config.ConfigManager
import dev.lyric.config.source.FolderConfigSource
import dev.lyric.income.sell.IncomeSellPlugin
import dev.lyric.income.sell.config.SellwandConfig
import dev.lyric.income.sell.utils.AdventureUtils.minimessage
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

object IncomeSellCommand {

	private val configManager: ConfigManager
		get() = IncomeSellPlugin.configManager
	private val sellwandFolderSource: FolderConfigSource<SellwandConfig>
		get() = configManager.getFolderSource("sellwands")!!

	fun registerCommand() {
		CommandTree("incomesell")
			.then(addReloadArgument())
			.then(addSellwandArgument())
			.register()
	}

	fun addReloadArgument(): Argument<*> {
		return LiteralArgument("reload").executes(
			CommandExecutor { sender, _ ->
				try {
					sender.sendMessage { " &8• &eAttempting to reload all plugin configurations.".minimessage() }
					IncomeSellPlugin.instance.reloadConfig()
				} catch (exception: Exception) {
					sender.sendMessage { " &8• &cAn internally exception occurred while reloading the configs.".minimessage() }
					exception.printStackTrace()
					return@CommandExecutor
				}
				sender.sendMessage { " &8• &aSuccessfully finished reloading all configs".minimessage() }
			},
			ExecutorType.PLAYER, ExecutorType.CONSOLE
		)
	}

	fun addSellwandArgument(): Argument<*> {
		return LiteralArgument("give-sellwand")
			.then(EntitySelectorArgument.OnePlayer("player")
			.then(createSellwandArgument("sellwand")
			.then(IntegerArgument("uses", 1).setOptional(true)
			.executes(CommandExecutor { sender, arguments ->
				val player = arguments.get("player") as? Player
				val sellwand = arguments.get("sellwand") as? SellwandConfig
				val uses = arguments.get("uses") as? Int
				if (player == null) {
					sender.sendMessage { " &8• &cCould not find a player from the given argument '${arguments.getRaw("player")}'".minimessage() }
					return@CommandExecutor
				}
				if (sellwand == null) {
					sender.sendMessage { " &8• &cCould not find a sellwand from the given argument '${arguments.getRaw("sellwand")}'".minimessage() }
					return@CommandExecutor
				}
				player.sendMessage { " &8• &aYou've been given a ${sellwand.identifier} sellwand!".minimessage() }
				if (uses == null) {
					player.give(sellwand.createUnlimitedUseItem())
				} else {
					player.give(sellwand.createLimitedUseItem(uses, uses))
				}
			}, ExecutorType.PLAYER, ExecutorType.CONSOLE))))
	}

	private fun createSellwandArgument(codename: String): Argument<SellwandConfig> {
		return CustomArgument(StringArgument(codename)) { info ->
			val sellwand = sellwandFolderSource.getChild(info.input())
				?: throw CustomArgument.CustomArgumentException.fromMessageBuilder(MessageBuilder("Unknown sellwand: ").appendArgInput())
			return@CustomArgument sellwand
		}.replaceSuggestions(ArgumentSuggestions.stringsAsync { _ ->
			CompletableFuture.supplyAsync { sellwandFolderSource.getKeys().toTypedArray() }
		})
	}

}