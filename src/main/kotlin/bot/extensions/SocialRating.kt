package bot.extensions

import bot.lib.Database
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.addReaction
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class SocialRating : Extension() {
	override val name = "social-rating"
	override val bundle = "cs_dsbot"

	private val rateLimitsCache = mutableMapOf<Long, Instant>()

	private fun rateLimit(from: Snowflake, to: Snowflake) {
		val now = Clock.System.now()

		val globalRateLimitTime = now + 15.minutes
		val globalRateLimitRowId = Database.addRateLimit(from, to, globalRateLimitTime)

		val localRateLimitTime = now + 12.hours
		val localRateLimitRowId = Database.addRateLimit(from, to, localRateLimitTime)

		rateLimitsCache[globalRateLimitRowId] = globalRateLimitTime
		rateLimitsCache[localRateLimitRowId] = localRateLimitTime
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

				if (Database.hasRateLimit(fromId))
					return@action event.message.addReaction("🕒")

				if (isPlusRep)
					Database.addRating(toId)
				else
					Database.removeRating(toId)

				rateLimit(fromId, toId)
				event.message.addReaction("✅")
			}
		}

		event<ReadyEvent> {
			while (true) {
				rateLimitsCache.forEach { (id, expireTime) ->
					val now = Clock.System.now()

					if (now >= expireTime) {
						Database.removeRateLimit(id)
						rateLimitsCache.remove(id)
					}
				}

				delay(1_000)
			}
		}

		publicMessageCommand {
			name = "Rollback rep"

			action {
				respond { content = "Rollback..." }
			}
		}
	}
}