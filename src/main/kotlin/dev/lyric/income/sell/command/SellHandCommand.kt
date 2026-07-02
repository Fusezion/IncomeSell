package dev.lyric.income.sell.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import dev.lyric.income.economy.api.EconomyCollection
import dev.lyric.income.sell.api.IncomeSellAPI
import dev.lyric.income.sell.utils.AdventureUtils.minimessage
import dev.lyric.income.sell.utils.SellUtils
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

object SellHandCommand {

	fun registerCommand() {
		CommandAPICommand("sellhand")
			.executesPlayer(PlayerCommandExecutor { player, _ ->
				val heldItem = player.equipment.itemInMainHand
				if (SellUtils.processItemSell(player, heldItem.clone()))
					heldItem.amount = 0
			})
			.register()
	}

}