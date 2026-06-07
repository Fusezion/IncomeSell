package dev.lyric.income.sell.config.entry

import kotlinx.serialization.KSerializer

interface ConfigEntry<T : Any> {

	val path: String
	val serializer: KSerializer<T>
	val default: T?
		get() = null
	val onSave: (T) -> Unit
		get() = {}
	val onLoad: (T) -> Unit
		get() = {}

	fun load()
	fun save()

	fun onSave(config: T) {}

	fun onLoad(config: T) {}

}