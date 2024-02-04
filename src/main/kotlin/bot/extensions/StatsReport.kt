package bot.extensions

import bot.botLogger
import bot.lib.Config
import bot.lib.Database
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.GuildMessageChannel
import kotlinx.coroutines.delay
import kotlinx.datetime.*
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@ExperimentalTime
class StatsReport : Extension() {
	override val name = "stats-report"
	override val bundle = "cs_dsbot"

	private val translationsProvider: TranslationsProvider by inject()

	override suspend fun setup() {
		val timeZone = TimeZone.of(Config.discord.timeZone)

		botLogger.info(translationsProvider.translate(
			"extensions.experience.stats.timeZone", bundle, arrayOf(timeZone.id)
		))

		val initMoment = Clock.System.now()
		val currentTime = initMoment.toLocalDateTime(timeZone)

		val initPublishTime = LocalDateTime(
			currentTime.year,	currentTime.month + 1, 1,
			0, 0
		).toInstant(timeZone)

		val initialDelay = initPublishTime - initMoment

		// Log start time
		botLogger.info(translationsProvider.translate(
			"extensions.experience.stats.reportLog", bundle, arrayOf(initPublishTime)
		))

		// Log initial delay
		botLogger.info(translationsProvider.translate(
			"extensions.experience.stats.logDelay", bundle, arrayOf(initialDelay)
		))

		Scheduler().schedule(initialDelay, repeat = true) {
			// Save start time
			val startTime = Clock.System.now()

			// Get channel from config
			val channel = kord.getChannelOf<GuildMessageChannel>(Snowflake(Config.discord.statsReportChannelId))
			if (channel == null) { // Unload if can't resolve channel
				botLogger.error("Failed to get channel! Unloading...")

				doUnload()
				return@schedule
			}

			// Get users from DB
			val users = Database.getTopUsers(10)

			// Send top
			channel.createMessage {
				embeds?.plusAssign(
					Experience.topEmbed(
						kord, translationsProvider, this@StatsReport.bundle, users, channel.guild, true
					)
				)
			}

			// Clear DB
			Database.cleanUp()

			// Zeroing joined users list
			Experience.joinedMembers.clear()

			channel.guild.voiceStates.collect {
				Experience.joinedMembers[it.getMember()] = startTime
			}

			// Wait 1 month
			delay(1.days * 365.2425 / 12)
		}
	}
}