@file:Suppress("UnstableApiUsage")

package dev.lyric.income.sell.config

import dev.lyric.income.sell.utils.AdventureUtils.miniMessage
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.UseCooldown
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.inventory.ItemStack

@Serializable
data class SellwandConfig(
	val multiplier: Float,
	val identifier: String,
	@SerialName("unlimited-uses-item")
	val unlimitedUsesItem: ItemStackConfig,
	@SerialName("limited-uses-item")
	val limitedUsesItem: ItemStackConfig,
) {

	private val multiResolver = Placeholder.unparsed("multi", multiplier.toString())

	fun getUnlimitedUsesItem(resolvers: Array<TagResolver> = emptyArray()): ItemStack {
		val updatedResolvers = resolvers.toMutableSet().also { it.add(multiResolver) }.toTypedArray()
		return unlimitedUsesItem.createItemStack(updatedResolvers).also {
			it.setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(0.25f).cooldownGroup(Key.key("incomesell:sellwand")))
		}
	}

	fun getLimitedUsesItem(resolvers: Array<TagResolver> = emptyArray()): ItemStack {
		val updatedResolvers = resolvers.toMutableSet().also { it.add(multiResolver) }.toTypedArray()
		return limitedUsesItem.createItemStack(updatedResolvers).also {
			it.setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(0.25f).cooldownGroup(Key.key("incomesell:sellwand")))
		}
	}

	fun getLimitedUseLore(resolvers: Array<TagResolver> = emptyArray()): List<Component> {
		val updatedResolvers = resolvers.toMutableSet().also { it.add(multiResolver) }.toTypedArray()
		return limitedUsesItem.lore.map { it.miniMessage(*updatedResolvers) }
	}

}