package dev.lyric.income.sell.commands

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.lyric.income.sell.api.IncomeSellAPI
import dev.lyric.income.sell.api.SellResult

object SellCommand {

	fun register() {

		commandAPICommand("sell") {
			withPermission("incomesell.command.sell")
			playerExecutor { player, _ ->
				val sellResult: SellResult = IncomeSellAPI.sellInventory(player.inventory)
				if (!sellResult.handlePayout(player)) return@playerExecutor
				player.sendMessage(sellResult.getSoldMessage())
			}
		}

	}
}