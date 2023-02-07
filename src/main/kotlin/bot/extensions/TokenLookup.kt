package bot.extensions

import bot.lib.discord_tokens.DiscordClient
import bot.lib.discord_tokens.dto.UserAccountObject
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.common.toMessageFormat
import dev.kord.rest.builder.message.create.embed
import io.ktor.util.*

class TokenLookup : Extension() {
	override val name = "token-lookup"
	override val bundle = "cs_dsbot"

	@InternalAPI
	override suspend fun setup() {
		ephemeralSlashCommand(::TokenArgs) {
			name = "extensions.tokenLookup.commandName"
			description = "extensions.tokenLookup.commandDescription"

			action {
				val api = DiscordClient(arguments.token)
				val user = api.fetchUser()
				if (user == null) {
					respond { content = "Wrong token, couldn't fetch info" }
					return@action
				}

				val guilds = api.fetchGuilds()

				respond {
					embed {
						author {
							name = if (api.isBot) "${user.fullName} [BOT]" else user.fullName
							icon = "https://cdn.discordapp.com/avatars/${user.id}/${user.avatar}.png"

							if (api.isBot)
								url = "https://discord.com/api/oauth2/authorize?client_id=${user.id.value}&scope=bot"
						}

						color = user.accentColor?.let(::Color)

						field("ID", true) { "${user.id.value}" }
						field("Language", true) { ":flag_${user.locale.country?.lowercase() ?: "black"}:" }
						field("Registered At", true) { user.id.timestamp.toMessageFormat() }

						user.email?.let { email -> field("Email", true) { email } }
						(user as? UserAccountObject)?.phone?.let { phone -> field("Phone", true) { phone } }

						field("Verified", true) { "\\" + if (user.verified) "✅" else "❌" }
						field("MFA", true) { "\\" + if (user.mfaEnabled) "✅" else "❌" }
						(user as? UserAccountObject)?.nsfwAllowed?.let { nsfw ->
							field("NSFW", true) { "\\" + if (nsfw) "✅" else "❌" }
						}

						user.bio.let { bio -> if (bio.isNotEmpty()) field("Bio") { bio } }

						field("Guilds") {
							guilds.take(10).joinToString("\n") { guild ->
								if (guild.owner) "\\👑 `${guild.name}`" else "`${guild.name}`"
							} + if(guilds.size > 10) "\n..." else ""
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