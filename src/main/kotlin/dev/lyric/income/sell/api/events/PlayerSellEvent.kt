package dev.lyric.income.sell.api.events

import dev.lyric.income.sell.api.SellResult
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerSellEvent(player: Player, val sellResult: SellResult) : PlayerEvent(player), Cancellable {

	private var cancelledState = false

	companion object {
		private val handlerList = HandlerList()

		fun getHandlerList() = handlerList
	}

	override fun isCancelled(): Boolean = cancelledState

	override fun setCancelled(cancel: Boolean) {
		cancelledState = cancel
	}

	override fun getHandlers(): HandlerList = handlerList
}