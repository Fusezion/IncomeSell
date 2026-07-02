package dev.lyric.income.sell

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import dev.lyric.config.ConfigManager
import dev.lyric.config.source.FileConfigSource
import dev.lyric.config.source.FolderConfigSource
import dev.lyric.income.sell.command.IncomeSellCommand
import dev.lyric.income.sell.command.SellCommand
import dev.lyric.income.sell.command.SellGuiCommand
import dev.lyric.income.sell.command.SellHandCommand
import dev.lyric.income.sell.config.SellGuiConfig
import dev.lyric.income.sell.config.SellwandConfig
import dev.lyric.income.sell.listener.SellwandListener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class IncomeSellPlugin : JavaPlugin() {

	companion object {
		lateinit var instance: IncomeSellPlugin
		lateinit var configManager: ConfigManager
	}

	override fun onLoad() {
		CommandAPI.onLoad(CommandAPIPaperConfig(this).setNamespace("incomesell").silentLogs(false).verboseOutput(true))
	}

	override fun onEnable() {
		CommandAPI.onEnable()
		instance = this
		configManager = ConfigManager(this)
		registerConfigs()
		registerCommands()
		registerListeners()
		CommandAPI.onEnable()
	}

	override fun onDisable() {
		CommandAPI.onDisable()
	}

	private fun registerCommands() {
		IncomeSellCommand.registerCommand()
		SellHandCommand.registerCommand()
		SellCommand.registerCommand()
		SellGuiCommand.registerCommand()
	}

	private fun registerConfigs() {
		val sellwandList = listOf("common.yml", "uncommon.yml", "rare.yml", "epic.yml", "legendary.yml", "mythic.yml")
		configManager.register(FolderConfigSource("sellwands", File("sellwands"), SellwandConfig.serializer(), sellwandList))
		configManager.register(FileConfigSource("sellgui", File("sellgui.yml"), SellGuiConfig.serializer()))
		configManager.loadAll()
	}

	private fun registerListeners() {
		val pluginManager = server.pluginManager
		pluginManager.registerEvents(SellwandListener(), this)
	}

	override fun reloadConfig() {
		configManager.loadAll()
	}
}