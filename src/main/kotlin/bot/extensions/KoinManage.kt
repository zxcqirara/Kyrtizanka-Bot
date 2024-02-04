package bot.extensions

import bot.lib.Config
import bot.lib.Database
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand

class KoinManage : Extension() {
	override val name = "koin-manage"
	override val bundle = "cs_dsbot"

	override suspend fun setup() {

		ephemeralSlashCommand {
			name = "extensions.koin.commandName"
			description = "extensions.koin.commandDescription"

			check {
				failIfNot(Database.hasSpecialAccess(kord, event.interaction.user.id))
			}

			ephemeralSubCommand(::NameArgs) {
				name = "extensions.koin.load.commandName"
				description = "extensions.koin.load.commandDescription"

				action {
					bot.loadExtension(arguments.name)

					Config.disabledExtensions -= arguments.name
					Config.dumpDisabledExtensions()

					respond { content = translate("extensions.koin.load.text", arrayOf(arguments.name)) }
				}
			}

			ephemeralSubCommand(::NameArgs) {
				name = "extensions.koin.unload.commandName"
				description = "extensions.koin.unload.commandDescription"

				action {
					bot.unloadExtension(arguments.name)

					Config.disabledExtensions += arguments.name
					Config.dumpDisabledExtensions()

					respond { content = translate("extensions.koin.unload.text", arrayOf(arguments.name)) }
				}
			}

			ephemeralSubCommand {
				name = "extensions.koin.list.commandName"
				description = "extensions.koin.list.commandDescription"

				action {
					val extensions = bot.extensions
						.map { (if (it.value.loaded) "+" else "-") + " ${it.key}" }
						.joinToString("\n")

					respond { content = translate("extensions.koin.list.text", arrayOf(extensions)) }
				}
			}
		}
	}

	inner class NameArgs : Arguments() {
		val name by string {
			name = "name"
			description = "extensions.koin.arguments.name"
		}
	}
}