package dev.lyric.income.sell

import dev.lyric.config.ConfigManager
import dev.lyric.config.source.FileConfigSource
import dev.lyric.config.source.FolderConfigSource
import dev.lyric.income.sell.commands.IncomeSellCommand
import dev.lyric.income.sell.commands.SellCommand
import dev.lyric.income.sell.commands.SellGuiCommand
import dev.lyric.income.sell.commands.SellHandCommand
import dev.lyric.income.sell.config.MessageConfig
import dev.lyric.income.sell.config.SellGuiConfig
import dev.lyric.income.sell.config.SellwandConfig
import dev.lyric.income.sell.listeners.SellGuiListener
import dev.lyric.income.sell.listeners.SellwandUseListener
import dev.lyric.income.sell.messages.Messages
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class IncomeSell : JavaPlugin() {

	companion object {
		lateinit var instance: IncomeSell
		lateinit var messages: Messages
		lateinit var configManager: ConfigManager
	}

	override fun onEnable() {
		instance = this
		registerConfigs()
		messages = Messages(configManager.getFile("messages")!!)
		registerCommands()
		registerListeners(server.pluginManager)
	}

	private fun registerListeners(pluginManager: PluginManager) {
		pluginManager.registerEvents(SellwandUseListener(), this)
		pluginManager.registerEvents(SellGuiListener(), this)
	}

	private fun registerConfigs() {
		configManager = ConfigManager(this)
		val sellwandFolderConfig = FolderConfigSource(
			"sellwands",
			File("sellwands"),
			SellwandConfig.serializer(),
			listOf("common.yml", "uncommon.yml", "rare.yml", "epic.yml", "legendary.yml", "mythic.yml")
		)
		configManager.register(sellwandFolderConfig)
		configManager.register(
			FileConfigSource(
				"messages",
				File("messages.yml"),
				MessageConfig.serializer(),
				MessageConfig()
			)
		)
		configManager.register(FileConfigSource("sellgui", File("sellgui.yml"), SellGuiConfig.serializer()))
		configManager.loadAll()
	}

	private fun registerCommands() {
		lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
			val commands = it.registrar()
			commands.register(SellCommand.createCommand())
			commands.register(SellHandCommand.createCommand())
			commands.register(SellGuiCommand.createCommand())
			commands.register(IncomeSellCommand.createCommand())
		}
	}

}
