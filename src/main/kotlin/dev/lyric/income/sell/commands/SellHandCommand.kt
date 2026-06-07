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

object SellHandCommand {

	val messages: Messages
		get() = IncomeSell.messages

	fun createCommand(): LiteralCommandNode<CommandSourceStack> {
		return Commands.literal("sellhand")
			.requires { (it.executor ?: it.sender).hasPermission("incomesell.command.sellhand") }
			.executes { context ->
				val source = context.source
				val executor = source.executor ?: source.sender
				if (executor !is Player) {
					executor.sendMessage(messages.resolve { command.executableByPlayers })
					return@executes Command.SINGLE_SUCCESS
				}
				val heldItem = executor.equipment.itemInMainHand
				if (heldItem.isEmpty) {
					executor.sendMessage(messages.resolve { sellHand.mustHoldItem })
					return@executes Command.SINGLE_SUCCESS
				}
				if (!IncomeSellAPI.hasSellActions(heldItem)) {
					executor.sendMessage(messages.resolve { sellHand.invalidItem })
					return@executes Command.SINGLE_SUCCESS
				}
				val sellResult = IncomeSellAPI.createEmptySellResult()
				sellResult.recordItemTransaction(
					heldItem.clone(),
					heldItem.amount,
					IncomeSellAPI.getSellActions(heldItem)
				)
				if (!IncomeSellAPI.payoutTransactions(executor, sellResult)) return@executes Command.SINGLE_SUCCESS
				val resolvers = arrayOf(
					MessageTagResolvers.formatTotalCurrency(
						sellResult,
						"currency_breakdown_short",
						NumberFormat.Style.SHORT
					),
					MessageTagResolvers.formatTotalCurrency(sellResult, "currency_breakdown", NumberFormat.Style.LONG),
					MessageTagResolvers.numberFormat("item_amount", heldItem.amount),
					MessageTagResolvers.hoverableItem(heldItem),
					MessageTagResolvers.breakdownTag(sellResult),
				)
				heldItem.amount = 0
				executor.sendMessage(messages.resolve({ sellHand.sellMessage }, *resolvers))
				return@executes Command.SINGLE_SUCCESS
			}
			.build()
	}

}