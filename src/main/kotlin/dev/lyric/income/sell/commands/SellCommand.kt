package dev.lyric.income.sell.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.tree.LiteralCommandNode
import dev.lyric.income.sell.IncomeSell
import dev.lyric.income.sell.api.IncomeSellAPI
import dev.lyric.income.sell.messages.MessageTagResolvers
import dev.lyric.income.sell.messages.Messages
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import java.text.NumberFormat

object SellCommand {

	val messages: Messages
		get() = IncomeSell.messages

	fun createCommand(): LiteralCommandNode<CommandSourceStack> {
		return Commands.literal("sell")
			.requires { sourceStack ->
				(sourceStack.executor ?: sourceStack.sender).hasPermission("incomesell.command.sell")
			}
			.executes { commandContext ->
				val source = commandContext.source
				val executor = source.executor ?: source.sender
				if (executor !is Player) {
					executor.sendMessage(messages.resolve { command.executableByPlayers })
					return@executes Command.SINGLE_SUCCESS
				}
				val sellResult = IncomeSellAPI.createEmptySellResult()
				sellResult.recordInventoryTransaction(executor.inventory)
				if (!sellResult.hasTransactions()) {
					executor.sendMessage(IncomeSell.messages.resolve { bulkSell.nothingSold })
					return@executes Command.SINGLE_SUCCESS
				}
				if (!IncomeSellAPI.payoutTransactions(executor, sellResult)) return@executes Command.SINGLE_SUCCESS
				val totalItemsSold = sellResult.getItemTransactions().sumOf { it.amountSold }
				val resolvers = arrayOf(
					MessageTagResolvers.numberFormat("total_items", totalItemsSold),
					MessageTagResolvers.formatTotalCurrency(sellResult, "currency_breakdown", NumberFormat.Style.LONG),
					MessageTagResolvers.formatTotalCurrency(
						sellResult,
						"currency_breakdown_short",
						NumberFormat.Style.SHORT
					),
					MessageTagResolvers.breakdownTag(sellResult)
				)
				executor.sendMessage(messages.resolve({ bulkSell.sellMessage }, *resolvers))
				executor.inventory.filterNotNull().filter(IncomeSellAPI::hasSellActions).forEach { it.amount = 0 }
				return@executes Command.SINGLE_SUCCESS
			}
			.build()
	}

}