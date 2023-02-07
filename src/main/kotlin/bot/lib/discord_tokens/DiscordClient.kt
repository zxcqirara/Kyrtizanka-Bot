package bot.lib.discord_tokens

import bot.lib.discord_tokens.dto.AccountObject
import bot.lib.discord_tokens.dto.BotAccountObject
import bot.lib.discord_tokens.dto.GuildObject
import bot.lib.discord_tokens.dto.UserAccountObject
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import kotlin.properties.Delegates

/**
 * API for lookup discord account by token
 *
 * @param token Account's token
 */
@InternalAPI
class DiscordClient(private val token: String) {
	private val client = HttpClient(CIO) {
		install(ContentNegotiation) {
			json(Json { ignoreUnknownKeys = true })
		}
	}
	private val baseUrl = "https://discord.com/api/v9"
	private val chromeAgent = "Chrome/109.0.0.0"

	lateinit var userData: AccountObject
	lateinit var guildsData: List<GuildObject>

	var isBot by Delegates.notNull<Boolean>()

	suspend fun fetchUser(): AccountObject? {
		var res = client.get("$baseUrl/users/@me") {
			userAgent(chromeAgent)
			header("Authorization", "Bot $token")
		}

		if (res.status == HttpStatusCode.Unauthorized) {
			res = client.get("$baseUrl/users/@me") {
				userAgent(chromeAgent)
				header("Authorization", token)
			}
		} else {
			userData = res.body<BotAccountObject>()
			isBot = true

			return userData
		}

		return if (res.status == HttpStatusCode.Unauthorized) null else {
			try {
				userData = res.body<BotAccountObject>()
				isBot = true
			}
			catch (e: Exception) {
				userData = res.body<UserAccountObject>()
				isBot = false
			}

			userData
		}
	}

	suspend fun fetchGuilds(): List<GuildObject> {
		val res = client.get("$baseUrl/users/@me/guilds") {
			userAgent(chromeAgent)

			val header = if (isBot && !token.startsWith("Bot")) "Bot $token" else token
			header("Authorization", header)
		}
		guildsData = res.body<List<GuildObject>>()

		return guildsData
	}
}