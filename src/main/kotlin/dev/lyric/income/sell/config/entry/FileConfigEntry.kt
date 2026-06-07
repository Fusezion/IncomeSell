package dev.lyric.income.sell.config.entry

import dev.lyric.income.sell.IncomeSell
import kotlinx.serialization.KSerializer
import net.mamoe.yamlkt.Yaml
import java.io.File

open class FileConfigEntry<T : Any>(
	override val path: String,
	override val serializer: KSerializer<T>,
	override val default: T? = null,
	override val onLoad: (T) -> Unit = {},
	override val onSave: (T) -> Unit = {}
) : ConfigEntry<T> {

	companion object {
		private val plugin = IncomeSell.instance
	}

	private lateinit var value: T

	override fun load() {
		val file = File(plugin.dataFolder, path)
		if (!file.exists()) {
			if (plugin.getResource(path) != null) {
				plugin.saveResource(path, false)
			} else if (default != null) {
				file.parentFile.mkdirs()
				file.writeText(Yaml.encodeToString(serializer, default!!))
			} else error("Missing config file $path and no default value supplies.")
		}
		value = Yaml.decodeFromString(serializer, file.readText())
		onLoad.invoke(value)
	}

	override fun save() {
		val file = File(plugin.dataFolder, path)
		file.parentFile.mkdirs()
		file.writeText(Yaml.encodeToString(serializer, value))
		onSave.invoke(value)
	}

	fun get(): T = value
}