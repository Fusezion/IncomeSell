package dev.lyric.income.sell.api.event

import dev.lyric.income.sell.api.SellReport
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PrePlayerSellEvent(val player: Player, val sellReport: SellReport) : Event(), Cancellable {

	companion object {

		@JvmField
		val handlerList = HandlerList()

		@JvmStatic
		fun getHandlerList(): HandlerList = handlerList

	}

	private var cancel: Boolean = false

	override fun getHandlers(): HandlerList {
		return handlerList
	}

	override fun isCancelled(): Boolean = cancel

	override fun setCancelled(cancel: Boolean) {
		this.cancel = cancel
	}

}
