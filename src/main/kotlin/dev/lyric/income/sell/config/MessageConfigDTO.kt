package dev.lyric.income.sell.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageConfig(
	@SerialName("sell-hand")
	val sellHand: SellhandMessageConfig = SellhandMessageConfig(),
	@SerialName("bulk-sell")
	val bulkSell: BulkSellMessageConfig = BulkSellMessageConfig(),
	val breakdown: BreakdownMessageConfig = BreakdownMessageConfig(),
	val command: CommandMessageConfig = CommandMessageConfig()
)

@Serializable
data class CommandMessageConfig(
	@SerialName("executable-by-players")
	val executableByPlayers: String = "&cThis command is only executable by players",
	val reload: String = "&aSuccessfully reloaded all configs",
	val invalidSellwand: String = "&cUnknown/Invalid sellwand <sellwand>, double check spelling",
	val noPlayerFound: String = "&cNo player found with provided argument selector",
	val playerReceivedSellwand: String = "&aYou've received a &f'<sellwand>'&a sellwand",
	val playerReceivedSellwandWithUses: String = "&aYou've received a &f'<sellwand>'&a sellwand with &f<uses>&a uses",
)

@Serializable
data class SellhandMessageConfig(
	@SerialName("must-hold-item")
	val mustHoldItem: String = "&cYou must be holding an item to use this command",
	@SerialName("invalid-item")
	val invalidItem: String = "&cThis item could not be sold",
	@SerialName("sell-message")
	val sellMessage: String = "&aYou've sold <item_amount>x <item_name> for &f<currency_breakdown_short>"
)

@Serializable
data class BulkSellMessageConfig(
	@SerialName("nothing-sold")
	val nothingSold: String = "&cThere was nothing to sell...",
	@SerialName("sell-message")
	val sellMessage: String = "&aYou sold <total_items> items for <currency_breakdown_short> <breakdown>&7[Hover]"
)

@Serializable
data class BreakdownMessageConfig(
	@SerialName("item-header")
	val itemHeader: String = "<!i>&7&lItem Breakdown:",
	@SerialName("item-format")
	val itemFormat: String = "<!i>&f<item_amount>x <item_name> &8→ &f<currency_breakdown>",
	@SerialName("multiplier-header")
	val multiplierHeader: String = "<!i>&7&lMultiplier Breakdown:",
	@SerialName("global-multiplier")
	val globalMultiplier: String = "<!i>&eGlobal Multiplier&7:&f <multiplier>",
	@SerialName("provider-with-multiplier")
	val providerWithMultiplier: String = "<!i>&e<display_name>&7:&f <multiplier>x",
	@SerialName("provider-without-multiplier")
	val providerWithoutMultiplier: String = "<!i>&e<display_name>&7:&f",
	@SerialName("provider-child-multiplier")
	val providerChildMultiplier: String = "<!i>  &e<display_name>&7:&f <multiplier>x"
)