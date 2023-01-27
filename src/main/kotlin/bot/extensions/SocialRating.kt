package bot.extensions

import bot.lib.Database
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalMember
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.addReaction
import com.kotlindiscord.kord.extensions.utils.delete
import com.kotlindiscord.kord.extensions.utils.deleteOwnReaction
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.reply
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class SocialRating : Extension() {
	override val name = "social-rating"
	override val bundle = "cs_dsbot"

	private val rateLimitsCache = mutableMapOf<Long, Instant>()

	private fun rateLimit(from: Snowflake, to: Snowflake) {
		val now = Clock.System.now()

		if (Database.getGlobalRateLimit(from).isEmpty()) {
			val localRateLimitTime = now + 12.hours
			val localRateLimitRowId = Database.addRateLimit(from, to, localRateLimitTime)
			rateLimitsCache[localRateLimitRowId] = localRateLimitTime
		}

		val globalRateLimitTime = now + 15.minutes
		val globalRateLimitRowId = Database.addRateLimit(from, null, globalRateLimitTime)
		rateLimitsCache[globalRateLimitRowId] = globalRateLimitTime
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
				val to = event.message.referencedMessage!!.author.takeUnless { it?.isBot ?: true } ?: return@action
				val toId = to.id

				val isPlusRep = when (event.message.content) {
					"+rep" -> true
					"-rep" -> false
					else -> return@action
				}

				val rateLimit = Database.getRateLimit(fromId, toId)

				if (rateLimit != null) {
					event.message.reply {
						content = translate(
							"extensions.socialRating.remainingTime",
							arrayOf(rateLimit.expire.toDiscord(TimestampType.RelativeTime))
						)
					}.delete(30_000)

					event.message.addReaction("🕒")
					return@action
				}

				if (
					Database.getUser(fromId).socialBlackList ||
					Database.getUser(toId).socialBlackList
				) return@action

				if (isPlusRep)
					Database.addRating(toId)
				else
					Database.removeRating(toId)

				rateLimit(fromId, toId)
				Database.addRepMessage(event.message, isPlusRep)

				event.message.addReaction("✅")
			}
		}

		event<ReadyEvent> {
			action {
				while (true) {
					rateLimitsCache.forEach { (id, expireTime) ->
						val now = Clock.System.now()

						if (now >= expireTime) {
							Database.removeRateLimit(id)
							rateLimitsCache.remove(id)
						}
					}

					delay(10_000)
				}
			}
		}

		ephemeralSlashCommand(::SRBlacklistArgs) {
			name = "extensions.socialRating.blacklist.commandName"
			description = "extensions.socialRating.blacklist.commandDescription"

			check {
				failIf(
					!Database.hasSpecialAccess(kord, event.interaction.user.id),
					translate("extensions.errors.specialAccess", this@SocialRating.bundle)
				)
			}

			action {
				if (arguments.member == null) {
					respond {
						embed {
							title = translate("extensions.socialRating.blacklist.embed.title")
							description = Database.getBlackListedUsers().map {
								event.kord.getUser(Snowflake(it.id.value))?.mention
									?: "*${translate("extensions.socialRating.blacklist.userNotFound")}*"
							}.joinToString("\n")
						}
					}
				}
				else {
					val user = arguments.member!!.asUser()

					if (Database.getBlackListedUsers().map { it.id.value }.contains(user.id.value.toLong())) {
						Database.removeUserFromBlackList(user.id)

						respond {
							content = translate("extensions.socialRating.blacklist.removed", arrayOf(user.mention))
						}
					}
					else {
						Database.addUserToBlackList(user.id)

						respond {
							content = translate("extensions.socialRating.blacklist.added", arrayOf(user.mention))
						}
					}
				}
			}
		}

		publicMessageCommand {
			name = "extensions.socialRating.rollback.buttonName"

			check {
				failIf(
					!Database.hasSpecialAccess(kord, event.interaction.user.id),
					translate("extensions.errors.specialAccess", this@SocialRating.bundle)
				)

				val repMessage = Database.getRepMessage(event.interaction.targetId)
				failIf(repMessage == null, translate("extensions.socialRating.errors.notRepMessage", bundle))
			}

			action {
				val eventInitiator = event.interaction.user
				val repMessage = Database.getRepMessage(event.interaction.targetId)!!

				if (repMessage.isPlus)
					Database.removeRating(Snowflake(repMessage.to))
				else
					Database.addRating(Snowflake(repMessage.to))

				Database.removeRepMessage(Snowflake(repMessage.id.value))
				event.interaction.target.deleteOwnReaction("✅")

				respond {
					content = translate("extensions.socialRating.rollback.success", this@SocialRating.bundle)

					components(60.seconds) {
						publicButton {
							style = ButtonStyle.Danger
							label = translate("extensions.socialRating.rollback.buttons.resetTimeout", this@SocialRating.bundle)

							check {
								failIf(event.interaction.user.id != eventInitiator.id)
							}

							action {
								Database.removeRateLimitsFrom(repMessage.from)
								rateLimitsCache.remove(repMessage.to)

								respond {
									content = translate(
										"extensions.socialRating.rollback.buttons.resetTimeout.success",
										this@SocialRating.bundle
									)
								}
							}
						}
					}
				}
			}
		}
	}

	inner class SRBlacklistArgs : Arguments() {
		val member by optionalMember {
			name = "member"
			description = "extensions.socialRating.blacklist.arguments.member"
		}
	}
}