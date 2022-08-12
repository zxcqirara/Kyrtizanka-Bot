package bot.extensions

import bot.lib.Database
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.addReaction
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.message.MessageCreateEvent
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class SocialRating : Extension() {
	override val name = "social-rating"
	override val bundle = "cs_dsbot"

	private val respectsScheduler = Scheduler()
	private val rateLimitedScheduler = Scheduler()

	private val recentRespects = mutableListOf<Respect>()
	private val rateLimited = mutableListOf<Snowflake>()

	private fun rateLimit(from: Snowflake, to: Snowflake) {
		recentRespects.add(Respect(from, to))
		rateLimited.add(from)

		respectsScheduler.schedule(12.hours, pollingSeconds = 3600) { // 12.hours, pollingSeconds = 3600
			recentRespects.remove(Respect(from, to))
		}

		rateLimitedScheduler.schedule(15.minutes, pollingSeconds = 60) { // 15.minutes, pollingSeconds = 60
			rateLimited.remove(from)
		}
	}

	override suspend fun setup() {
		event<MessageCreateEvent> {
			check {
				failIf(event.message.referencedMessage == null)
				failIf(event.message.author == event.message.referencedMessage?.author)
				isNotBot()
				failIf(event.member?.isBot ?: true)
			}

			action {
				val fromId = event.message.author?.id ?: return@action
				val to = event.message.referencedMessage!!.author ?: return@action
				if (to.isBot) return@action
				val toId = to.id

				val isPlusRep = when (event.message.content) {
					"+rep" -> true
					"-rep" -> false
					else -> return@action
				}
			}
		}
	}
}