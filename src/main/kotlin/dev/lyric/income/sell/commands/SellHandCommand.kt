package dev.lyric.income.sell.commands

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.lyric.income.sell.api.IncomeSellAPI
import dev.lyric.income.sell.utils.AdventureUtils.component
import net.kyori.adventure.text.format.NamedTextColor

object SellHandCommand {

	fun register() {
		commandAPICommand("sellhand") {
			withPermission("incomesell.command.sellhand")
			playerExecutor { player, _ ->
				val heldItem = player.equipment.itemInMainHand
				if (heldItem.isEmpty) {
					player.sendMessage { "You must hold some type of item in your hand".component(NamedTextColor.RED) }
					return@playerExecutor
				}
				if (!IncomeSellAPI.hasSellActions(heldItem)) {
					player.sendMessage { "The item in your hand could not be sold".component(NamedTextColor.RED) }
					return@playerExecutor
				}
				val sellResult = IncomeSellAPI.sellItem(heldItem)
				if (!sellResult.handlePayout(player)) return@playerExecutor
				player.sendMessage(sellResult.getSoldMessage())
			}
		}
	}

}