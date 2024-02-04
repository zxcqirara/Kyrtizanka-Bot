package bot.extensions

import bot.lib.Config
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class CurrentTime : Extension() {
	override val name = "current-time"
	override val bundle = "cs_dsbot"

	override suspend fun setup() {
		publicSlashCommand {
			name = "extensions.currentTime.commandName"
			description = "extensions.currentTime.commandDescription"

			action {
				val now = Clock.System.now()
				val pretty = now.toLocalDateTime(TimeZone.of(Config.discord.timeZone))

				respond {
					content = pretty.toString()
				}
			}
		}
	}
}