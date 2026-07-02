package dev.lyric.income.sell.utils

import dev.lyric.income.sell.utils.StringUtils.toTinyCaps
import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.CharacterAndFormat
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

object AdventureUtils {

	private val tinyCapsResolver = TagResolver.resolver("tinycaps") { arguments, context ->
		if (!arguments.hasNext()) throw context.newException("Missing string argument for tinycaps")
		val stringValue = arguments.pop().value()
		return@resolver Tag.preProcessParsed(stringValue.toTinyCaps())
	}

	private val miniMessage = MiniMessage.builder()
		.editTags { it.resolver(tinyCapsResolver) }
		.preProcessor { sourceString ->
			var outputString = sourceString
			for (charFormat in CharacterAndFormat.defaults()) {
				val char = charFormat.character()
				@Suppress("LiftReturnOrAssignment")
				if (char == 'r') {
					outputString = outputString.replace(Regex("[&§]$char", RegexOption.IGNORE_CASE), "<reset>")
				} else {
					outputString = outputString.replace(Regex("[&§]$char", RegexOption.IGNORE_CASE), "<${charFormat.format()}>")
				}
			}
			return@preProcessor outputString.replace(Regex("&#([a-f0-9]{6})", RegexOption.IGNORE_CASE), "<#$1>")
		}
		.postProcessor { component -> component.decoration(TextDecoration.ITALIC, false ) }
		.build()

	private val jsonSerializer = GsonComponentSerializer.gson()
	private val plainSerializer = PlainTextComponentSerializer.plainText()

	fun String.minimessage() = miniMessage.deserialize(this)

	fun String.minimessage(vararg resolvers: TagResolver) = miniMessage.deserialize(this, *resolvers)

	fun String.minimessage(pointered: Pointered) = miniMessage.deserialize(this, pointered)

	fun String.minimessage(pointered: Pointered, vararg resolvers: TagResolver) = miniMessage.deserialize(this, pointered, *resolvers)

	fun String.component() = Component.text(this)

	fun String.component(style: Style) = Component.text(this, style)

	fun String.component(color: TextColor) = Component.text(this, color)

	fun String.component(color: TextColor, vararg decorations: TextDecoration) = Component.text(this, color, *decorations)

	fun Component.serialize() = miniMessage.serialize(this)

	fun Component.jsonString() = jsonSerializer.serialize(this)

	fun Component.plainString() = plainSerializer.serialize(this)

	fun String.jsonComponent() = jsonSerializer.deserialize(this)

	fun String.plainComponent() = plainSerializer.deserialize(this)

}