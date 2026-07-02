package dev.lyric.income.sell.api

import dev.lyric.income.economy.api.EconomyCollection
import org.bukkit.inventory.ItemStack

data class ItemTransaction(
	val item: ItemStack,
	val itemAmount: Int = 0,
	val transactions: EconomyCollection
)