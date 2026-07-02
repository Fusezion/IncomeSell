package dev.lyric.income.sell.config

import dev.lyric.income.sell.config.data.ItemStackConfig
import dev.lyric.income.sell.utils.PDCKeys
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.UseCooldown
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@Suppress("UnstableApiUsage")
@Serializable
data class SellwandConfig(
	val multiplier: Float,
	val cooldown: Float = 0.2f,
	val identifier: String,
	@SerialName("unlimited-use-item")
	val unlimitedUseItem: ItemStackConfig,
	@SerialName("limited-use-item")
	val limitedUseItem: ItemStackConfig
) {

	fun createLimitedUseItem(uses: Int, maxUses: Int, resolvers: Array<TagResolver> = emptyArray()): ItemStack {
		val updatedResolvers = resolvers.toMutableSet().apply {
			add(Formatter.number("multiplier", multiplier))
			add(Formatter.number("cooldown", cooldown))
			add(Formatter.number("uses", uses))
			add(Formatter.number("max_uses", maxUses))
		}.toTypedArray()
		val sellwand = limitedUseItem.createItemStack(updatedResolvers)
		sellwand.editPersistentDataContainer { container ->
			val sellwandPDC = container.adapterContext.newPersistentDataContainer()
			sellwandPDC.set(PDCKeys.sellwandType, PersistentDataType.STRING, identifier)
			sellwandPDC.set(PDCKeys.sellwandUses, PersistentDataType.INTEGER, uses)
			sellwandPDC.set(PDCKeys.sellwandMaxUses, PersistentDataType.INTEGER, maxUses)
			container.set(PDCKeys.sellwand, PersistentDataType.TAG_CONTAINER, sellwandPDC)
		}
		sellwand.setData(DataComponentTypes.MAX_DAMAGE, maxUses)
		sellwand.setData(DataComponentTypes.DAMAGE, maxUses-uses)
		sellwand.setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(cooldown).cooldownGroup(PDCKeys.sellwandCooldown))
		return sellwand
	}

	fun createUnlimitedUseItem(resolvers: Array<TagResolver> = emptyArray()): ItemStack {
		val updatedResolvers = resolvers.toMutableSet().apply {
			add(Formatter.number("multiplier", multiplier))
			add(Formatter.number("cooldown", cooldown))
		}.toTypedArray()
		val sellwand = unlimitedUseItem.createItemStack(updatedResolvers)
		sellwand.editPersistentDataContainer { container ->
			val sellwandPDC = container.adapterContext.newPersistentDataContainer()
			sellwandPDC.set(PDCKeys.sellwandType, PersistentDataType.STRING, identifier)
			container.set(PDCKeys.sellwand, PersistentDataType.TAG_CONTAINER, sellwandPDC)
		}
		sellwand.setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(cooldown).cooldownGroup(PDCKeys.sellwandCooldown))
		return sellwand
	}

}
