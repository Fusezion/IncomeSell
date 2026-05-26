package dev.lyric.income.sell.commands

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.lyric.income.sell.sellgui.SellGui

object SellGuiCommand {

	fun register() {
		commandAPICommand("sellgui") {
			withPermission("incomesell.command.sellgui")
			playerExecutor { player, _ ->
				val gui = SellGui.createGui(player)
				player.openInventory(gui.guiInventory)
			}
		}
	}

}