package bot

import bot.database.experience.Experiences
import bot.database.meme.Memes
import bot.database.rating.RatingRateLimits
import bot.database.rep_messages.RepMessages
import bot.database.tag.Tags
import bot.database.user.Users
import bot.extensions.*
import bot.lib.Config
import bot.lib.ConfigDto
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import com.typesafe.config.ConfigRenderOptions
import dev.kord.common.annotation.KordVoice
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.NON_PRIVILEGED
import dev.kord.gateway.PrivilegedIntent
import io.github.config4k.toConfig
import mu.KotlinLogging
import okio.FileSystem
import okio.Path.Companion.toPath
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exposedLogger
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime
import org.jetbrains.exposed.sql.Database as KtDatabase

val botLogger = KotlinLogging.logger("Bot")

@KordVoice
@ExperimentalTime
@PrivilegedIntent
suspend fun main() {
	val configPath = Config.PATH.toPath()

	if (!FileSystem.SYSTEM.exists(configPath)) {
		botLogger.warn("Config not found, creating...")

		val renderOptions = ConfigRenderOptions.defaults()
			.setJson(false)
			.setOriginComments(false)

		FileSystem.SYSTEM.write(configPath, true) {
			writeUtf8(
				ConfigDto.Discord().toConfig("discord")
					.root().render(renderOptions)
			)

			writeUtf8("\n")

			writeUtf8(
				ConfigDto.Database().toConfig("database")
					.root().render(renderOptions)
			)
		}

		botLogger.warn("Configure the config!")
		exitProcess(1)
	}

	Config.update()

	val dbConfig = Config.database

	KtDatabase.connect(
		url = "jdbc:pgsql://${dbConfig.url}/${dbConfig.database}",
		user = dbConfig.username,
		password = dbConfig.password,
		driver = "com.impossibl.postgres.jdbc.PGDriver"
	)

	transaction {
		SchemaUtils.create(
			Users,
			RatingRateLimits,
			Experiences,
			Memes,
			Tags,
			RepMessages
		)
	}

	val discordConfig = Config.discord
	val bot = ExtensibleBot(discordConfig.token) {
		intents {
			+ Intents.NON_PRIVILEGED
			+ Intent.MessageContent
		}

		i18n {
			defaultLocale = SupportedLocales.ALL_LOCALES[discordConfig.language] ?: SupportedLocales.ENGLISH

			interactionUserLocaleResolver()
		}

		presence {
			val version = javaClass.classLoader.getResource("version.txt")!!.readText()
			playing("${discordConfig.game} | $version")
		}

		chatCommands {
			enabled = true
			prefix { "~" }
		}

		applicationCommands {
			defaultGuild(discordConfig.guildId)
		}

		extensions {
			if (discordConfig.sentryLink.isNotEmpty())
				sentry {
					dsn = discordConfig.sentryLink
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
			add(::Statistic)
			add(::ReloadExtension)
			add(::TokenLookup)
			add(::CurrentTime)
			add(::AutoRegion)
		}
	}

	Config.loadDisabledExtensions()
	Config.disabledExtensions.forEach { extensionName ->
		bot.unloadExtension(extensionName)
	}

	bot.start()
}