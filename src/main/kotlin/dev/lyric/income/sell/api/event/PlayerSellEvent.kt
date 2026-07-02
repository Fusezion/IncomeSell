package dev.lyric.income.sell.api.event

import dev.lyric.income.sell.api.SellReport
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerSellEvent(val player: Player, val sellReport: SellReport) : Event() {

	companion object {

		@JvmField
		val handlerList = HandlerList()

		@JvmStatic
		fun getHandlerList(): HandlerList = handlerList

	}

	override fun getHandlers(): HandlerList = handlerList

}
