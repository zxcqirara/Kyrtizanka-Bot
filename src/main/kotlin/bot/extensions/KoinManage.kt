package bot.extensions

import bot.lib.Database
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.types.respond
import org.koin.core.component.inject

class KoinManage : Extension() {
	override val name = "koin_manage"
	override val bundle = "cs_dsbot"

	private val translationsProvider: TranslationsProvider by inject()

	override suspend fun setup() {

		ephemeralSlashCommand {
			name = "extensions.koin.commandName"
			description = "extensions.koin.commandDescription"

			check {
				failIfNot(Database.hasSpecialAccess(kord, event.interaction.user.id))
			}

			ephemeralSubCommand(::NameArgs) {
				name = "load" // extensions.koin.load.commandName
				description = "extensions.koin.load.commandDescription"

				action {
					bot.loadExtension(arguments.name)

					respond { content = translate("extensions.koin.load.text", arrayOf(arguments.name)) }
				}
			}

			ephemeralSubCommand(::NameArgs) {
				name = "unload" // extensions.koin.unload.commandName
				description = "Unload module" // extensions.koin.unload.commandDescription

				action {
					bot.unloadExtension(arguments.name)

					respond { content = translate("extensions.koin.unload.text", arrayOf(arguments.name)) }
				}
			}

			ephemeralSubCommand {
				name = "list" // extensions.koin.list.commandName
				description = "List of modules" // extensions.koin.list.commandDescription

				action {
					println(bot.extensions)

					val exts = bot.extensions
						.map { (if (it.value.loaded) "+" else "-") + " ${it.key}" }
						.joinToString("\n")

					respond { content = translate("extensions.koin.list.text", arrayOf(exts)) }
				}
			}
		}
	}

	inner class NameArgs : Arguments() {
		val name by string {
			name = "name"
			description = translationsProvider.translate("extensions.koin.arguments.name")
		}
	}
}