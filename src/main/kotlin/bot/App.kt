package bot

import bot.database.experience.Experiences
import bot.database.meme.Memes
import bot.database.rating.RatingRateLimits
import bot.database.rep_messages.RepMessages
import bot.database.user.Users
import bot.database.tag.Tags
import bot.extensions.*
import bot.lib.ConfigFile
import com.charleskorn.kaml.Yaml
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import dev.kord.common.annotation.KordVoice
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Database as KtDatabase
import java.io.File
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime

@KordVoice
@ExperimentalTime
@PrivilegedIntent
suspend fun main() {
	val initedConfig = readConfig()
	val dbData = initedConfig.database

	KtDatabase.connect(
		url = "jdbc:pgsql://${dbData.url}/${dbData.database}",
		user = dbData.username,
		password = dbData.password,
		driver = "com.impossibl.postgres.jdbc.PGDriver"
	)

	transaction {
		SchemaUtils.create(Users, RatingRateLimits, Experiences, Memes, Tags, RepMessages)
	}

	val bot = ExtensibleBot(initedConfig.token) {
		intents {
			+ Intents.nonPrivileged
			+ Intent.MessageContent
		}

		i18n {
			defaultLocale = SupportedLocales.RUSSIAN

			interactionUserLocaleResolver()
		}

		presence {
			val version = javaClass.classLoader.getResource("version.txt")!!.readText()
			playing("${initedConfig.game} | $version")
		}

		chatCommands {
			enabled = true
			prefix { "~" }
		}

		applicationCommands {
			defaultGuild(initedConfig.guildId)
		}

		extensions {
			if (initedConfig.sentryLink.isNotEmpty()) {
				sentry {
					dsn = initedConfig.sentryLink
				}
			}

			add(::PingCommand)
			add(::Reminder)
			add(::Votes)
			add(::Scripting)
			add(::KoinManage)
			add(::Experience)
			add(::Fun)
			add(::Music)
			add(::Info)
			add(::Tuts)
			add(::StatsReport)
			add(::MemeScore)
			add(::SocialRating)
			add(::Tags)
			add(::PrivateRooms)
		}
	}

	bot.start()
}

suspend fun readConfig(): ConfigFile {

	val configFile = withContext(Dispatchers.IO) {
		val file = File("config.yml")

		if (!file.exists()) {
			Yaml.default.encodeToStream(ConfigFile.serializer(), ConfigFile(), file.outputStream())

			println("Config created, configure please!")
			exitProcess(0)
		}

		return@withContext file
	}

	val readedConfig = runCatching {
		Yaml.default.decodeFromStream(ConfigFile.serializer(), configFile.inputStream())
	}
		.onFailure {
			println("Can't read config! Delete config file & restart the program!")

			exitProcess(1)
		}
		.getOrThrow()

	return readedConfig
}