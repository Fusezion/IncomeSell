package dev.lyric.income.sell.api

import org.bukkit.inventory.ItemStack

data class ItemTransaction(
	val item: ItemStack,
	var amountSold: Int = 0,
	val transactions: MutableMap<String, Double> = mutableMapOf()
)