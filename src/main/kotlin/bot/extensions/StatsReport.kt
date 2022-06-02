package bot.extensions

import bot.lib.Database
import bot.readConfig
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.kordLogger
import kotlinx.coroutines.delay
import kotlinx.datetime.*
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@ExperimentalTime
class StatsReport : Extension() {
	override val name = "stats-report"

	private val translationsProvider: TranslationsProvider by inject()

	override suspend fun setup() {
		val timeOffset = TimeZone.of("Europe/Moscow")

		val initMoment = Clock.System.now()
		val currentTime = initMoment.toLocalDateTime(timeOffset)

		val initPublishTime = LocalDateTime(
			currentTime.year,	currentTime.month + 1, 1,
			0, 0
		).toInstant(timeOffset)

		val initialDelay = initPublishTime - initMoment

		// Log start time
		kordLogger.info(translationsProvider.translate(
			"extensions.experience.stats.reportLog",
			replacements = arrayOf(initPublishTime)
		))

		// Log initial delay
		kordLogger.info(translationsProvider.translate(
			"extensions.experience.stats.logDelay",
			replacements = arrayOf(initialDelay)
		))

		Scheduler().schedule(initialDelay, repeat = true) {
			// Save start time
			val startTime = Clock.System.now()

			// Get channel from config
			val channel = kord.getChannelOf<GuildMessageChannel>(Snowflake(readConfig().statsReportChannelId))
			if (channel == null) { // Unload if can't resolve channel
				kordLogger.error("Failed to get channel! Unloading...")

				doUnload()
				return@schedule
			}

			// Get users from DB
			val users = Database.getTopUsers(10)

			// Send top
			channel.createMessage {
				embeds += Experience.topEmbed(kord, translationsProvider, users, channel.guild, true)
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