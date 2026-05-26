package dev.lyric.income.sell.providers

import dev.lyric.income.sell.api.provider.SellProvider
import org.bukkit.entity.Player
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI
import su.nightexpress.excellenteconomy.api.currency.operation.NotificationTarget
import su.nightexpress.excellenteconomy.api.currency.operation.OperationContext

class ExcellentEconomyProvider(private val economyAPI: ExcellentEconomyAPI) : SellProvider {

	private val operationContext = OperationContext.custom("IncomeSell").silentFor(NotificationTarget.EXECUTOR, NotificationTarget.USER)
	override val identifier: String = "excellent_economy"
	override val displayName: String = "ExcellentEconomy"

	override fun handleSell(player: Player, extraArg: String?, amount: Double) {
		if (extraArg == null || !economyAPI.hasCurrency(extraArg)) return
		economyAPI.depositAsync(player.uniqueId, extraArg, amount, operationContext)
	}

	override fun displayString(extraArg: String?, amount: Double): String {
		if (extraArg == null || !economyAPI.hasCurrency(extraArg)) return amount.toString()
		return economyAPI.getCurrency(extraArg)!!.formatCompact(amount)
	}

	override fun displayArgument(extraArg: String?): String? {
		val currency = economyAPI.getCurrency(extraArg ?: return null) ?: return null
		return currency.name
	}

}