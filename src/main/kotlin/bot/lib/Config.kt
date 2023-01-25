package bot.lib

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import java.io.File

object Config {
	const val path = "config"

	lateinit var discord: ConfigDto.Discord
	lateinit var database: ConfigDto.Database

	fun update() {
		val config = ConfigFactory.parseFile(File(path))
		discord = config.extract("discord")
		database = config.extract("database")
	}

	private val disabledExtensionsFile = File("disabled.txt")
	val disabledExtensions = mutableSetOf<String>()

	fun loadDisabledExtensions() {
		with(disabledExtensionsFile) {
			if (!exists()) {
				createNewFile()

				return@with
			}

			disabledExtensions.addAll(disabledExtensionsFile.readLines())
		}
	}

	fun dumpDisabledExtensions() {
		disabledExtensionsFile.writeText(
			disabledExtensions.joinToString("\n")
		)
	}
}