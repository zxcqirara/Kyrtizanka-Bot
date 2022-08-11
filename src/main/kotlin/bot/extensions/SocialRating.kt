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

	private val antiSpamScheduler = Scheduler()
	private val respects = mutableListOf<Respect>()
	private val ratelimited = mutableListOf<Snowflake>()

	private fun rateLimit(from: Snowflake, to: Snowflake) {
		respects.add(Respect(from, to))
		ratelimited.add(from)

		antiSpamScheduler.schedule(12.hours, pollingSeconds = 3600) { // 12.hours, pollingSeconds = 3600
			respects.remove(Respect(from, to))
		}

		antiSpamScheduler.schedule(15.minutes, pollingSeconds = 60) { // 15.minutes, pollingSeconds = 60
			ratelimited.remove(from)
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

				when (event.message.content) {
					"+rep" -> {
						if (
							respects.find { it.from == fromId }?.to == toId ||
							ratelimited.contains(fromId)
						) {
							event.message.addReaction("🕒")
							return@action
						}

						Database.addRating(toId)
						rateLimit(fromId, toId)

						event.message.addReaction("✅")
					}

					"-rep" -> {
						if (
							respects.find { it.from == fromId }?.to == toId ||
							ratelimited.contains(fromId)
						) {
							event.message.addReaction("🕒")
							return@action
						}

						Database.removeRating(toId)
						rateLimit(fromId, toId)

						event.message.addReaction("✅")
					}
				}
			}
		}
	}

	private data class Respect(
		val from: Snowflake,
		val to: Snowflake
	)
}