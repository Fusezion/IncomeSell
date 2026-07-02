package dev.lyric.income.sell.utils

object StringUtils {

	private val tinyCapsMap = mapOf(
		'a' to 'ᴀ', 'b' to 'ʙ', 'c' to 'ᴄ',
		'd' to 'ᴅ', 'e' to 'ᴇ', 'f' to 'ꜰ',
		'g' to 'ɢ', 'h' to 'ʜ', 'i' to 'ɪ',
		'j' to 'ᴊ', 'k' to 'ᴋ', 'l' to 'ʟ',
		'm' to 'ᴍ', 'n' to 'ɴ', 'o' to 'ᴏ',
		'p' to 'ᴘ', 'q' to 'ǫ', 'r' to 'ʀ',
		's' to 's', 't' to 'ᴛ', 'u' to 'ᴜ',
		'v' to 'ᴠ', 'w' to 'ᴡ', 'x' to 'x',
		'y' to 'ʏ', 'z' to 'ᴢ'
	)

	fun String.toTinyCaps(): String {
		val outputString = StringBuilder()
		var index = 0

		while (index < length) {
			val char = this[index]

			// Handles <anything in between>
			if (char == '<') {
				val startIndex = index
				index++
				while (index < length && this[index] != '>') {
					index++
				}
				if (index < length) index++
				outputString.append(this.substring(startIndex, index))
				continue
			}
			// Handles &#RRGGBB
			if (char == '&' && (index+1 < length && this[index+1] == '#') && index+7 < length) {
				val startIndex = index
				index += 7
				outputString.append(this.substring(startIndex, index))
				continue
			}
			// Handles &[a-z0-9]
			if ((char == '&' || char == '§') && index+1 < length) {
				val next = this[index+1].lowercaseChar()
				if (next in tinyCapsMap) {
					outputString.append(char).append(next)
					index += 2
					continue
				}
			}
			// Handles \anything
			if (char == '\\' && index+1 < length) {
				outputString.append(char).append(this[index+1])
				index += 2
				continue
			}
			outputString.append(tinyCapsMap[char.lowercaseChar()] ?: char)
			index++
		}

		return outputString.toString()
	}

}