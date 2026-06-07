package dev.lyric.income.sell.commands.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.lyric.income.sell.config.ConfigManager
import dev.lyric.income.sell.config.data.SellwandConfig
import dev.lyric.income.sell.config.entry.FolderConfigEntry
import dev.lyric.income.sell.utils.AdventureUtils.component
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import java.util.concurrent.CompletableFuture

class SellwandArgumentType : CustomArgumentType<SellwandConfig, String> {

	private val sellwandConfigEntry: FolderConfigEntry<SellwandConfig>
		get() = ConfigManager.getFolderConfig("sellwands")

	private val invalidSellwandException = DynamicCommandExceptionType { input ->
		return@DynamicCommandExceptionType MessageComponentSerializer.message()
			.serialize("$input is not a valid sellwand name".component())
	}

	override fun parse(reader: StringReader): SellwandConfig {
		val sellwandName = reader.readUnquotedString()
		if (!sellwandConfigEntry.contains(sellwandName)) throw invalidSellwandException.create(sellwandName)
		return sellwandConfigEntry[sellwandName]
	}

	override fun <S : Any> listSuggestions(
		context: CommandContext<S>,
		builder: SuggestionsBuilder
	): CompletableFuture<Suggestions> {
		sellwandConfigEntry.getFileKeys().filter { it.startsWith(builder.remainingLowerCase, ignoreCase = true) }
			.forEach(builder::suggest)
		return builder.buildFuture()
	}

	override fun getNativeType(): ArgumentType<String> {
		return StringArgumentType.word()
	}

}
