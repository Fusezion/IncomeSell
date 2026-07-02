package dev.lyric.income.sell.utils

import org.bukkit.NamespacedKey

object PDCKeys {

	val sellActions = NamespacedKey.fromString("incomesell:sell_actions")!!
	val sellwand = NamespacedKey.fromString("incomesell:sellwand")!!
	val sellwandType = NamespacedKey.fromString("incomesell:type")!!
	val sellwandUses = NamespacedKey.fromString("incomesell:uses")!!
	val sellwandMaxUses = NamespacedKey.fromString("incomesell:max_uses")!!
	val sellwandCooldown = NamespacedKey.fromString("incomesell:sellwand_cooldown")!!

}