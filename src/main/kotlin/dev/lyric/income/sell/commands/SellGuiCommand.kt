package dev.lyric.income.sell.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.tree.LiteralCommandNode
import dev.lyric.income.sell.IncomeSell
import dev.lyric.income.sell.messages.Messages
import dev.lyric.income.sell.sellgui.SellGui
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

object SellGuiCommand {

	val messages: Messages
		get() = IncomeSell.messages

	fun createCommand(): LiteralCommandNode<CommandSourceStack> {
		return Commands.literal("sellgui")
			.requires { (it.executor ?: it.sender).hasPermission("incomesell.command.sellgui") }
			.executes { context ->
				val source = context.source
				val executor = source.executor ?: source.sender
				if (executor !is Player) {
					executor.sendMessage(messages.resolve { command.executableByPlayers })
					return@executes Command.SINGLE_SUCCESS
				}
				executor.openInventory(SellGui.createGui().inventory)
				return@executes Command.SINGLE_SUCCESS
			}
			.build()
	}

}