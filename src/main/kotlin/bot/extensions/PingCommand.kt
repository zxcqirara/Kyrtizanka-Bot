package bot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import kotlin.time.ExperimentalTime

@ExperimentalTime
class PingCommand : Extension() {
	override val name = "ping"

	override suspend fun setup() {
		publicSlashCommand {
			name = "extensions.ping.commandName"
			description = "extensions.ping.commandDescription"

			action {
				respond {
					embed {
						title = translate("extensions.ping.embed.title")
						description = buildString {
							append(translate("extensions.ping.embed.description.gateway"))
							appendLine("`${this@PingCommand.kord.gateway.averagePing}`")
						}
					}
				}
			}
		}
	}
}