package dev.lyric.income.sell.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import dev.lyric.income.economy.api.EconomyCollection
import dev.lyric.income.sell.api.IncomeSellAPI
import dev.lyric.income.sell.utils.AdventureUtils.minimessage
import dev.lyric.income.sell.utils.SellUtils
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

object SellCommand {
	
	fun registerCommand() {
		CommandAPICommand("sell")
			.executesPlayer(PlayerCommandExecutor { player, _ ->
				if (SellUtils.processInventorySell(player, player.inventory)) {
					player.inventory.filterNotNull()
						.filter { IncomeSellAPI.hasSellData(it) }
						.forEach { it.amount = 0 }
				}
			})
			.register()
	}
	
}