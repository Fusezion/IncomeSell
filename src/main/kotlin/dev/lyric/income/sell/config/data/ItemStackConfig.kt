package dev.lyric.income.sell.config.data

import dev.lyric.income.sell.config.serializers.BukkitColorSerializer
import dev.lyric.income.sell.config.serializers.DataComponentTypeSerializer
import dev.lyric.income.sell.config.serializers.EnchantmentSerializer
import dev.lyric.income.sell.config.serializers.ItemTypeSerializer
import dev.lyric.income.sell.config.serializers.KeySerializer
import dev.lyric.income.sell.utils.AdventureUtils.minimessage
import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.DyedItemColor
import io.papermc.paper.datacomponent.item.ItemEnchantments
import io.papermc.paper.datacomponent.item.ItemLore
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Color
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType

@Serializable
data class ItemStackConfig(
	@Serializable(with = ItemTypeSerializer::class) val material: ItemType,
	val amount: Int = 1,
	@SerialName("name") val customName: String? = null,
	@SerialName("item-name") val itemName: String? = null,
	val lore: List<String> = emptyList(),
	val unbreakable: Boolean = false,
	@SerialName("max-stack-size") val maxStackSize: Int? = null,
	@SerialName("enchantment-glint-override") val enchantmentGlintOverride: Boolean? = null,
	@Serializable(with = KeySerializer::class) @SerialName("item-model") val itemModel: Key? = null,
	@Serializable(with = BukkitColorSerializer::class) @SerialName("dyed-color") val dyedColor: Color? = null,
	@Serializable(with = KeySerializer::class) @SerialName("tooltip-style") val tooltipStyle: Key? = null,
	val enchantments: Map<@Serializable(with = EnchantmentSerializer::class) Enchantment, Int> = emptyMap(),
	val profile: ResolvableProfileConfig? = null,
	@SerialName("tooltip-display") val tooltipDisplay: TooltipDisplayConfig? = null,
	@SerialName("custom-model-data") val customModelData: CustomModelDataConfig? = null,
	@SerialName("removed-components") val removedComponents: List<@Serializable(with = DataComponentTypeSerializer::class) DataComponentType> = emptyList()
) {

	fun createItemStack(resolvers: Array<TagResolver> = emptyArray()): ItemStack {
		val itemStack = material.createItemStack(amount)
		if (customName.isNullOrBlank().not())
			itemStack.setData(DataComponentTypes.CUSTOM_NAME, customName.minimessage(*resolvers))
		if (itemName.isNullOrBlank().not())
			itemStack.setData(DataComponentTypes.ITEM_NAME, itemName.minimessage(*resolvers))
		if (lore.isNotEmpty())
			itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(lore.map { it.minimessage(*resolvers) }))
		if (unbreakable)
			itemStack.setData(DataComponentTypes.UNBREAKABLE)
		if (enchantmentGlintOverride != null)
			itemStack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, enchantmentGlintOverride)
		if (maxStackSize != null)
			itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, maxStackSize.coerceIn(1, 99))
		if (itemModel != null)
			itemStack.setData(DataComponentTypes.ITEM_MODEL, itemModel)
		if (dyedColor != null)
			itemStack.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(dyedColor))
		if (tooltipStyle != null)
			itemStack.setData(DataComponentTypes.TOOLTIP_STYLE, tooltipStyle)
		if (enchantments.isNotEmpty())
			itemStack.setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments(enchantments))
		if (profile != null)
			itemStack.setData(DataComponentTypes.PROFILE, profile.toResolvableProfile())
		if (tooltipDisplay != null)
			itemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltipDisplay.toTooltipDisplay())
		if (customModelData != null)
			itemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData.toCustomModelData())
		if (removedComponents.isNotEmpty())
			removedComponents.forEach(itemStack::unsetData)
		return itemStack
	}

}