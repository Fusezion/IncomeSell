package dev.lyric.income.sell.commands.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.lyric.config.source.FolderConfigSource
import dev.lyric.income.sell.IncomeSell
import dev.lyric.income.sell.config.SellwandConfig
import dev.lyric.income.sell.utils.AdventureUtils.component
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import java.util.concurrent.CompletableFuture

class SellwandArgumentType : CustomArgumentType<SellwandConfig, String> {

	private val sellwandConfigSource: FolderConfigSource<SellwandConfig>
		get() = IncomeSell.configManager.getFolderSource("sellwands")!!

	private val invalidSellwandException = DynamicCommandExceptionType { input ->
		return@DynamicCommandExceptionType MessageComponentSerializer.message()
			.serialize("$input is not a valid sellwand name".component())
	}

	override fun parse(reader: StringReader): SellwandConfig {
		val sellwandName = reader.readUnquotedString()
		if (sellwandConfigSource.getChild(sellwandName) == null)
			throw invalidSellwandException.create(sellwandName)
		return sellwandConfigSource.getChild(sellwandName)!!
	}

	override fun <S : Any> listSuggestions(
		context: CommandContext<S>,
		builder: SuggestionsBuilder
	): CompletableFuture<Suggestions> {
		sellwandConfigSource.getKeys().filter { it.startsWith(builder.remainingLowerCase, ignoreCase = true) }
			.forEach(builder::suggest)
		return builder.buildFuture()
	}

	override fun getNativeType(): ArgumentType<String> {
		return StringArgumentType.word()
	}

}
