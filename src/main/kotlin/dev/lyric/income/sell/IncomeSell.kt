package dev.lyric.income.sell

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import dev.lyric.income.sell.api.IncomeSellAPI
import dev.lyric.income.sell.commands.SellCommand
import dev.lyric.income.sell.commands.SellGuiCommand
import dev.lyric.income.sell.commands.SellHandCommand
import dev.lyric.income.sell.commands.SellwandCommand
import dev.lyric.income.sell.config.ConfigManager
import dev.lyric.income.sell.config.data.SellwandConfig
import dev.lyric.income.sell.config.entry.FolderConfigEntry
import dev.lyric.income.sell.listeners.SellGuiListener
import dev.lyric.income.sell.listeners.SellwandUseListener
import dev.lyric.income.sell.providers.ExcellentEconomyProvider
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI

class IncomeSell : JavaPlugin() {

	companion object {
		lateinit var instance: IncomeSell
	}

	override fun onLoad() {
		CommandAPI.onLoad(CommandAPIPaperConfig(this).silentLogs(false).verboseOutput(true).setNamespace("incomesell"))
	}

	override fun onEnable() {
		CommandAPI.onEnable()
		instance = this
		registerConfigs()
		registerCommands()
		registerDefaultProviders()
		registerListeners(server.pluginManager)
	}

	override fun onDisable() {
		CommandAPI.onDisable()
	}

	private fun registerListeners(pluginManager: PluginManager) {
		pluginManager.registerEvents(SellwandUseListener(), this)
		pluginManager.registerEvents(SellGuiListener(), this)
	}

	private fun registerConfigs() {
		val sellwandFolderConfig = FolderConfigEntry("sellwands", SellwandConfig.serializer(), listOf("common", "uncommon", "rare", "epic", "legendary", "mythic"))
		ConfigManager.registerFolder("sellwands", sellwandFolderConfig)
		ConfigManager.loadAllConfigFiles()
	}

	private fun registerCommands() {
		SellwandCommand.register()
		SellCommand.register()
		SellHandCommand.register()
		SellGuiCommand.register()
	}

	private fun registerDefaultProviders() {
		val economyProvider = server.servicesManager.getRegistration(ExcellentEconomyAPI::class.java) ?: return
		IncomeSellAPI.registerProvider(ExcellentEconomyProvider(economyProvider.provider))
	}

}
