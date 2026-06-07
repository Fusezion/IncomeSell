package dev.lyric.income.sell.messages

import dev.lyric.income.sell.config.data.MessageConfig
import dev.lyric.income.sell.utils.AdventureUtils.miniMessage
import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

class Messages(private val messageConfig: MessageConfig) {

	fun resolve(message: MessageConfig.() -> String): Component {
		return messageConfig.message().miniMessage()
	}

	fun resolveList(message: MessageConfig.() -> List<String>): Component {
		return messageConfig.message().map { it.miniMessage() }.reduce { acc, component -> acc.appendNewline().append(component) }
	}

	fun resolve(message: MessageConfig.() -> String, vararg resolvers: TagResolver): Component {
		return messageConfig.message().miniMessage(*resolvers)
	}

	fun resolveList(message: MessageConfig.() -> List<String>, vararg resolvers: TagResolver): Component {
		return messageConfig.message().map { it.miniMessage(*resolvers) }.reduce { acc, component -> acc.appendNewline().append(component) }
	}

	fun resolve(message: MessageConfig.() -> String, pointer: Pointered, vararg resolvers: TagResolver): Component {
		return messageConfig.message().miniMessage(pointer, *resolvers)
	}

	fun resolveList(message: MessageConfig.() -> List<String>, pointer: Pointered, vararg resolvers: TagResolver): Component {
		return messageConfig.message().map { it.miniMessage(pointer, *resolvers) }.reduce { acc, component -> acc.appendNewline().append(component) }
	}

}