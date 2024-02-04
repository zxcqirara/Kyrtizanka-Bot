package bot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import dev.kord.rest.builder.message.embed

class Info : Extension() {
	override val name = "info"
	override val bundle = "cs_dsbot"

	override suspend fun setup() {
		publicSlashCommand {
			name = "extensions.info.commandName"
			description = "extensions.info.commandDescription"

			action {
				respond {
					embed {
						title = translate("extensions.info.embed.title")

						description = translate("extensions.info.embed.description")
					}
				}
			}
		}
	}
}