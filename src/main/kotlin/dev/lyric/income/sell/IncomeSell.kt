package dev.lyric.income.sell

import dev.lyric.income.sell.commands.IncomeSellCommand
import dev.lyric.income.sell.commands.SellCommand
import dev.lyric.income.sell.commands.SellGuiCommand
import dev.lyric.income.sell.commands.SellHandCommand
import dev.lyric.income.sell.config.ConfigManager
import dev.lyric.income.sell.config.data.MessageConfig
import dev.lyric.income.sell.config.data.SellGuiConfig
import dev.lyric.income.sell.config.data.SellwandConfig
import dev.lyric.income.sell.config.entry.FileConfigEntry
import dev.lyric.income.sell.config.entry.FolderConfigEntry
import dev.lyric.income.sell.listeners.SellGuiListener
import dev.lyric.income.sell.listeners.SellwandUseListener
import dev.lyric.income.sell.messages.Messages
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin

class IncomeSell : JavaPlugin() {

	companion object {
		lateinit var instance: IncomeSell
		lateinit var messages: Messages
	}

	override fun onEnable() {
		instance = this
		registerConfigs()
		registerCommands()
		registerListeners(server.pluginManager)
	}

	private fun registerListeners(pluginManager: PluginManager) {
		pluginManager.registerEvents(SellwandUseListener(), this)
		pluginManager.registerEvents(SellGuiListener(), this)
	}

	private fun registerConfigs() {
		val sellwandFolderConfig = FolderConfigEntry(
			"sellwands",
			SellwandConfig.serializer(),
			listOf("common", "uncommon", "rare", "epic", "legendary", "mythic")
		)
		ConfigManager.registerFolder("sellwands", sellwandFolderConfig)
		val messagesFileConfig = FileConfigEntry(
			"messages.yml",
			MessageConfig.serializer(),
			MessageConfig(),
			onLoad = { messages = Messages(it) })
		ConfigManager.registerFile("messages", messagesFileConfig)
		ConfigManager.registerFile("sellgui", FileConfigEntry("sellgui.yml", SellGuiConfig.serializer()))
		ConfigManager.loadAllConfigEntries()
		messages = Messages(ConfigManager.getConfig("messages"))
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
