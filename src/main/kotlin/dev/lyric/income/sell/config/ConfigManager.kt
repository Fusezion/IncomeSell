package dev.lyric.income.sell.config

import dev.lyric.income.sell.config.entry.FileConfigEntry
import dev.lyric.income.sell.config.entry.FolderConfigEntry

object ConfigManager {

	private val folderConfigs = mutableMapOf<String, FolderConfigEntry<*>>()
	private val fileConfigs = mutableMapOf<String, FileConfigEntry<*>>()

	fun registerFolder(key: String, folder: FolderConfigEntry<*>) {
		this.folderConfigs[key] = folder
	}

	fun registerFile(key: String, file: FileConfigEntry<*>) {
		this.fileConfigs[key] = file
	}

	// =======[                            ]=======
	// =======[      Get Config Files      ]=======
	// =======[                            ]=======

	fun <T> getConfig(file: String): T {
		return this.fileConfigs[file]?.get() as T
	}

	fun <T> getConfigFromFolder(folder: String, file: String): T {
		return this.folderConfigs[folder]?.get(file) as T
	}

	fun <T : Any> getFolderConfig(folder: String): FolderConfigEntry<T> {
		return this.folderConfigs[folder] as FolderConfigEntry<T>
	}

	fun <T : Any> getFileConfig(file: String): FileConfigEntry<T> {
		return fileConfigs[file] as FileConfigEntry<T>
	}

	fun getAllConfigKeys(deep: Boolean = true): List<String> {
		val configKeys = fileConfigs.keys.toMutableList()
		for ((key, folder) in folderConfigs) {
			configKeys.add(key)
			if (deep) configKeys.addAll(folder.getFileKeys().map { "$key/$it" })
		}
		return configKeys.toList()
	}

	// =======[                             ]=======
	// =======[    Validate Config Files    ]=======
	// =======[                             ]=======

	fun isValidFile(path: String): Boolean {
		val pathNoExtension = path.removeSuffix(".yml")
		if (fileConfigs[pathNoExtension] != null) return true
		val folderFile = pathNoExtension.split("/", limit = 2)
		if (folderFile.size != 2 || folderFile.any(String::isBlank)) return false
		if (folderConfigs[folderFile[0]] != null && folderConfigs[folderFile[0]]!!.contains(folderFile[1])) return true
		return false
	}

	fun isValidFileConfig(file: String): Boolean {
		val fileNoExtension = file.removeSuffix(".yml")
		return fileConfigs[fileNoExtension] != null
	}

	fun isValidFolderConfig(path: String): Boolean {
		val pathNoExtension = path.removeSuffix(".yml")
		return folderConfigs[pathNoExtension] != null
	}

	fun isValidFolderConfigChild(path: String): Boolean {
		val pathNoExtension = path.removeSuffix(".yml")
		val folderFile = pathNoExtension.split("/", limit = 2)
		if (folderFile.size != 2 || folderFile.any(String::isBlank)) return false
		if (isValidFolderConfig(folderFile[0]) && folderConfigs[folderFile[0]]!!.contains(folderFile[1])) return true
		return false
	}

	// =======[                            ]=======
	// =======[  Load/Reload Config Files  ]=======
	// =======[                            ]=======

	fun loadAllConfigFiles() {
		this.fileConfigs.values.forEach(FileConfigEntry<*>::load)
		this.folderConfigs.values.forEach(FolderConfigEntry<*>::load)
	}

	fun loadConfigFile(file: String) {
		fileConfigs[file]?.load()
	}

	fun loadConfigFolder(folder: String) {
		folderConfigs[folder]?.load()
	}

	fun loadConfigFileFromFolder(folder: String, file: String) {
		folderConfigs[folder]?.loadChild(file)
	}

	// =======[                             ]=======
	// =======[      Save Config Files      ]=======
	// =======[                             ]=======

	fun saveAllConfigFiles() {
		this.fileConfigs.values.forEach(FileConfigEntry<*>::save)
		this.folderConfigs.values.forEach(FolderConfigEntry<*>::save)
	}

	fun saveConfigFile(file: String) {
		fileConfigs[file]?.save()
	}

	fun saveConfigFolder(folder: String) {
		folderConfigs[folder]?.save()
	}

	fun saveConfigFileFromFolder(folder: String, file: String) {
		folderConfigs[folder]?.saveChild(file)
	}

}