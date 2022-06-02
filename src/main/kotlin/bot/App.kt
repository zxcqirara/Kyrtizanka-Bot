package bot

import bot.extensions.*
import bot.lib.ConfigFile
import bot.lib.Database
import com.charleskorn.kaml.Yaml
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import dev.kord.common.annotation.KordVoice
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database as KtDatabase
import org.ktorm.support.postgresql.PostgreSqlDialect
import java.io.File
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime

@KordVoice
@ExperimentalTime
@PrivilegedIntent
suspend fun main() {
	val initedConfig = readConfig()
	val dbData = initedConfig.database

	Database.database = KtDatabase.Companion.connect(
		url = "jdbc:postgresql://${dbData.url}/${dbData.database}",
		user = dbData.username,
		password = dbData.password,
		dialect = PostgreSqlDialect(),
		driver = "org.postgresql.Driver"
	)

	val bot = ExtensibleBot(initedConfig.token) {
		intents {
			+ Intents.nonPrivileged
			+ Intent.MessageContent
		}

		i18n {
			defaultLocale = SupportedLocales.ENGLISH

			interactionUserLocaleResolver()
		}

		presence {
			playing("Genshin Impact")
		}

		chatCommands {
			enabled = true
			prefix { "~" }
		}

		applicationCommands {
			defaultGuild(initedConfig.guildId)
		}

		extensions {
			sentry {
				dsn = "https://b0af7b2f9c8748378bcc49f1a197a5cf@o1205879.ingest.sentry.io/6336610"
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