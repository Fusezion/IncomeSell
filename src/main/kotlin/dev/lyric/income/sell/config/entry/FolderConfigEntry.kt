package dev.lyric.income.sell.config.entry

import dev.lyric.income.sell.IncomeSell
import kotlinx.serialization.KSerializer
import net.mamoe.yamlkt.Yaml
import java.io.File

open class FolderConfigEntry<T : Any>(
	override val path: String,
	override val serializer: KSerializer<T>,
	private val internalFiles: List<String> = emptyList(),
	override val onSave: (T) -> Unit = {},
	override val onLoad: (T) -> Unit = {},
) : ConfigEntry<T> {

	companion object {
		private val plugin = IncomeSell.instance
	}

	private var values = mutableMapOf<String, T>()

	operator fun get(file: String): T = values[file]!!

	fun contains(file: String) = values.contains(file)

	fun getFileKeys(): Set<String> = values.keys.toSet()

	override fun load() {
		val folder = File(plugin.dataFolder, path)
		if (!folder.exists()) {
			folder.mkdirs()
			for (internalFile in internalFiles) {
				val fileName = internalFile.removeSuffix(".yml") + ".yml"
				if (plugin.getResource("$path/$fileName") != null) {
					plugin.saveResource("$path/$fileName", false)
				}
			}
		}
		values.clear()
		val files = folder.walkTopDown().filter { it.isFile && it.extension == "yml" }
		for (file in files) {
			val relativePath = folder.toPath().relativize(file.toPath()).toString().removeSuffix(".yml")
			val serializedValue = Yaml.decodeFromString(serializer, file.readText())
			values[relativePath] = serializedValue
			onLoad.invoke(serializedValue)
		}
	}

	override fun save() {
		values.keys.forEach(::saveChild)
	}

	fun loadChild(child: String) {
		val file = File(plugin.dataFolder, "$path/$child")
		if (values[child] == null && !file.exists()) return
		val serializedValue = Yaml.decodeFromString(serializer, file.readText())
		values[child] = serializedValue
		onLoad.invoke(serializedValue)
	}

	fun saveChild(child: String) {
		val value = values[child] ?: return
		val file = File(plugin.dataFolder, path)
		file.parentFile.mkdirs()
		file.writeText(Yaml.encodeToString(serializer, value))
		onSave.invoke(value)
	}

}