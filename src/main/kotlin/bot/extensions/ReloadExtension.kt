package bot.extensions

import bot.lib.Config
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

class ReloadExtension : Extension() {
	override val name = "reload"
	override val bundle = "cs_dsbot"

	override suspend fun setup() {
		ephemeralSlashCommand {
			name = "extensions.reload.commandName"
			description = "extensions.reload.commandDescription"
			allowByDefault = false

			action {
				Config.update()

				respond { content = translate("extensions.reload.success") }
			}
		}
	}
}