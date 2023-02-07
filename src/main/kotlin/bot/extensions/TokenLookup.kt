package bot.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.toMessageFormat
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.service.RestClient

class TokenLookup : Extension() {
	override val name = "token-lookup"
	override val bundle = "cs_dsbot"

	override suspend fun setup() {
		ephemeralSlashCommand(::TokenArgs) {
			name = "extensions.tokenLookup.commandName" // "extensions.tokenLookup.commandName"
			description = "extensions.tokenLookup.commandDescription" // "extensions.tokenLookup.commandDescription"

			action {
				val api = RestClient(arguments.token)

				val user = runCatching { api.user.getCurrentUser() }
					.getOrElse { respond { content = "Can't fetch user" }; return@action }

				val guilds = runCatching { api.user.getCurrentUserGuilds() }
					.getOrNull()

				respond {
					embed {
						author {
							name = "${user.username}#${user.discriminator}"
							if (user.bot.discordBoolean) name += " [BOT]"

							icon = "https://cdn.discordapp.com/avatars/${user.id}/${user.avatar}.png"
						}

						field("ID") { "${user.id.value}" }
						field("Locale") { "${user.locale.value}" }
						field("Registered At") { user.id.timestamp.toMessageFormat() }

						field("Guilds") {
							if (guilds != null)
								"```" + guilds.joinToString("\n") { it.name } + "```"
							else
								"Can't fetch guilds"
						}
					}
				}
			}
		}
	}

	inner class TokenArgs : Arguments() {
		val token by string {
			name = "token"
			description = "extensions.tokenLookup.arguments.token"
		}
	}
}