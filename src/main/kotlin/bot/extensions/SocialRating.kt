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

	private val antiSpamScheduler = Scheduler()
	private val respected = mutableListOf<Respected>()
	private val ratelimited = mutableListOf<Snowflake>()

	private fun rateLimit(from: Snowflake, to: Snowflake) {
		respected.add(Respected(from, to))
		ratelimited.add(from)

		antiSpamScheduler.schedule(12.hours, pollingSeconds = 3600) { // 12.hours, pollingSeconds = 3600
			respected.remove(Respected(from, to))
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
				failIf(event.message.author?.isBot ?: true)
			}

			action {
				val from = event.message.author?.id ?: return@action
				val to = event.message.referencedMessage!!.author?.id ?: return@action

				when (event.message.content) {
					"+rep" -> {
						if (
							respected.find { it.from == from }?.to == to ||
							ratelimited.contains(from)
						) {
							event.message.addReaction("🕒")
							return@action
						}

						Database.addRating(to)
						rateLimit(from, to)

						event.message.addReaction("✅")
					}

					"-rep" -> {
						if (
							respected.find { it.from == from }?.to == to ||
							ratelimited.contains(from)
						) {
							event.message.addReaction("🕒")
							return@action
						}

						Database.removeRating(to)
						rateLimit(from, to)

						event.message.addReaction("✅")
					}
				}
			}
		}
	}

	private data class Respected(
		val from: Snowflake,
		val to: Snowflake
	)
}