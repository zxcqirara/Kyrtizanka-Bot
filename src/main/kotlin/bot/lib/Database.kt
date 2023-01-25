package bot.lib

import bot.database.experience.Experience
import bot.database.experience.Experiences
import bot.database.meme.Meme
import bot.database.rating.RatingRateLimit
import bot.database.rating.RatingRateLimits
import bot.database.rep_messages.RepMessage
import bot.database.user.User
import bot.database.user.Users
import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Message
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.roundToLong

object Database {
	/**
	 * Add experience to member
	 *
	 * @param bot Bot object
	 * @param userId User ID
	 * @param count XP count
	 * @param moment Time moment that xp will added
	 *
	 * @return Event, that can give you more info
	 */
	suspend fun addExperience(
		bot: ExtensibleBot,
		userId: Long,
		count: Short,
		moment: Instant
	): XpUpdateEvent = newSuspendedTransaction {
		val nowHas = User.findById(userId)

		if (nowHas == null) {
			val xpEvent = XpUpdateEvent(count.toLong(), 0)
			bot.send(xpEvent)

			transaction {
				User.new(userId) {
					experience = count.toLong() * socialRating

					if (xpEvent.needUpdate)
						level = xpEvent.newLevel
				}

				Experience.new {
					this.userId = userId
					this.count = count
					time = moment.toJavaInstant()
				}
			}

			xpEvent
		} else {
			val multiplied = (if (nowHas.socialRating > 0)
				count * nowHas.socialRating
			else
				count / -nowHas.socialRating).toShort()

			val newExp = nowHas.experience + multiplied

			val xpEvent = XpUpdateEvent(newExp, nowHas.level)
			bot.send(xpEvent)

			transaction {
				val userUpdate = User.findById(userId)!!
				userUpdate.experience = newExp

				if (xpEvent.needUpdate)
					userUpdate.level = xpEvent.newLevel

				Experience.new {
					this.userId = userId
					this.count = count
					time = moment.toJavaInstant()
					multiplier = nowHas.socialRating / 10f
				}
			}

			xpEvent
		}
	}

	/*fun removeExperience(
		userId: Long,
		count: Short,
		moment: Instant
	) = transaction {
		val nowHas = User.findById(userId)

		if (nowHas != null) {
			nowHas.experience -= count

			val experienceUpdate = Experience.find { Experiences.userId eq userId }.first()
			experienceUpdate.count = (experienceUpdate.count - count).toShort()
			experienceUpdate.time = moment.toJavaInstant()
			experienceUpdate.multiplier = nowHas.socialRating / 10f
		}
		else {
			Experience.new {
				this.userId = userId
				this.count = (-count).toShort()
				time = moment.toJavaInstant()
			}
		}
	}*/

	/**
	 * Get user by ID
	 *
	 * @param userId User ID
	 */
	fun getUser(userId: Snowflake): User {
		val longUserId = userId.value.toLong()

		val data = transaction {
			User.findById(longUserId) ?: User.new(longUserId) {}
		}

		return data
	}

	/**
	 * Add user to social blacklist
	 *
	 * @param userId User ID
	 */
	fun addUserToBlackList(userId: Snowflake) {
		val longId = userId.value.toLong()

		transaction {
			val user = User.find { Users.id eq longId }
				.firstOrNull()

			if (user != null)
				user.socialBlackList = true
			else
				User.new(longId) { socialBlackList = true }
		}
	}

	/**
	 * Remove user from social blacklist
	 *
	 * @param userId User ID
	 */
	fun removeUserFromBlackList(userId: Snowflake) {
		val longId = userId.value.toLong()

		transaction {
			val user = User.find { Users.id eq longId }
				.firstOrNull()

			if (user != null)
				user.socialBlackList = false
			else
				User.new(longId) { socialBlackList = false }
		}
	}

	/**
	 * Get list of blacklisted users
	 */
	fun getBlackListedUsers() = transaction {
		User.find { Users.socialBlackList eq true }.toList()
	}

	/**
	 * Clean up database
	 */
	fun cleanUp() {
		transaction {
			Users.deleteAll()
			Experiences.deleteAll()
		}
	}

	/**
	 * Get top users
	 *
	 * @param count Count of users will be returned from top
	 */
	fun getTopUsers(count: Int): List<User> {
		return transaction {
			User.all()
				.sortedByDescending { it.experience }
				.take(count)
		}
	}

	/**
	 * Add voice seconds to user
	 *
	 * @param userId User ID
	 * @param count Count of seconds will be added
	 */
	suspend fun addSeconds(userId: Snowflake, count: Long) {
		val userIdLong = userId.value.toLong()
		val perSecond = Config.discord.experience.perSecond

		transaction {
			val userUpdate = User.findById(userIdLong) ?: User.new {}
			userUpdate.voiceTime += count
			userUpdate.experience += (count * perSecond).roundToLong()
		}
	}

	/**
	 * Check have user special accesss
	 *
	 * @param kord Kord instance (need for check)
	 * @param userId User ID
	 */
	suspend fun hasSpecialAccess(kord: Kord, userId: Snowflake): Boolean {
		val user = transaction {
			User.findById(userId.value.toLong())
		} ?: return false

		return user.specialAccess || kord.getApplicationInfo().ownerId == userId
	}

	/**
	 * Give special access to user
	 *
	 * @param userId User ID
	 */
	fun giveSpecialAccess(userId: Snowflake) {
		val userIdLong = userId.value.toLong()

		transaction {
			User.findById(userIdLong)?.run { specialAccess = true }
				?: User.new(userIdLong) { specialAccess = true }
		}
	}

	/**
	 * Take special access from user
	 *
	 * @param userId User ID
	 */
	fun takeSpecialAccess(userId: Snowflake) {
		transaction {
			User.findById(userId.value.toLong())
				?.specialAccess = false
		}
	}

	/**
	 * Get list of users with special access
	 */
	fun getUsersWithSpecialAccess() = transaction {
		User.find { Users.specialAccess eq true }.toList()
	}

	/**
	 * Returns need use emoji for user
	 */
	fun useEmoji(userId: Snowflake) = transaction {
		User.findById(userId.value.toLong())
			?.useEmoji ?: true
	}

	/**
	 * Add rating to user
	 *
	 * @param userId User ID
	 */
	fun addRating(userId: Snowflake) {
		val idLong = userId.value.toLong()

		transaction {
			val current = User.findById(idLong)

			if (current == null) {
				User.new(idLong) {
					socialRating = 11
				}
			}
			else {
				val newRating = if (current.socialRating == -2) 1 else current.socialRating + 1

				current.socialRating = newRating
			}
		}
	}

	/**
	 * Remove rating from user
	 *
	 * @param userId User ID
	 */
	fun removeRating(userId: Snowflake) {
		val idLong = userId.value.toLong()

		transaction {
			val current = User.findById(idLong)

			if (current == null) {
				User.new(idLong) {
					socialRating = 9
				}
			}
			else {
				val newRating = if (current.socialRating == 1) -2 else current.socialRating - 1

				current.socialRating = newRating
			}
		}
	}

	/**
	 * Add rating cool-down
	 *
	 * @param from User, who respected
	 * @param to User, who was respected
	 */
	fun addRateLimit(from: Snowflake, to: Snowflake?, expireTime: Instant): Long {
		val fromId = from.value.toLong()
		val toId = to?.value?.toLong()

		return transaction {
			val action = RatingRateLimit.new {
				this.from = fromId
				this.to = toId
				this.expire = expireTime.toJavaInstant()
			}

			return@transaction action.id.value
		}
	}

	/**
	 * Remove cool-down from user by row ID
	 *
	 * @param id ID of row
	 */
	fun removeRateLimit(id: Long) {
		transaction {
			RatingRateLimit.findById(id)?.delete()
		}
	}

	/**
	 * Remove cool-downs from user
	 *
	 * @param from From user ID
	 */
	fun removeRateLimitsFrom(from: Long) {
		transaction {
			RatingRateLimit.find { RatingRateLimits.from eq from }
				.forEach { it.delete() }
		}
	}

	/**
	 * Check if user has rate-limit for respect another user
	 *
	 * @param from User, who respected
	 * @param to User, who was respected
	 */
	fun hasRateLimit(from: Snowflake, to: Snowflake) = transaction {
		val fromId = from.value.toLong()
		val toId = to.value.toLong()

		(RatingRateLimit.find {
			(RatingRateLimits.from eq fromId) and (RatingRateLimits.to eq toId)
		}.firstOrNull() ?: RatingRateLimit.find { RatingRateLimits.from eq fromId }.firstOrNull()) != null
	}

	/**
	 * Check if user has global rate-limit
	 *
	 * @param userId User ID
	 */
	fun hasGlobalRateLimit(userId: Snowflake) = transaction {
		RatingRateLimit.find {
			(RatingRateLimits.from eq userId.value.toLong()) and (RatingRateLimits.to eq null)
		}.count() > 0
	}

	/**
	 * Add rep message in DB
	 *
	 * @param message Message ID
	 * @param isPlus Indicates that message is plus or minus rep
	 */
	fun addRepMessage(message: Message, isPlus: Boolean) {
		transaction {
			RepMessage.new(message.id.value.toLong()) {
				from = message.author!!.id.value.toLong()
				to = message.referencedMessage!!.author!!.id.value.toLong()
				this.isPlus = isPlus
			}
		}
	}

	/**
	 * Remove rep message from DB
	 *
	 * @param messageId Message ID
	 */
	fun removeRepMessage(messageId: Snowflake) {
		transaction {
			RepMessage.findById(messageId.value.toLong())?.delete()
		}
	}

	/**
	 * Get rep message by ID
	 *
	 * @param messageId Message ID
	 */
	fun getRepMessage(messageId: Snowflake) = transaction {
		RepMessage.findById(messageId.value.toLong())
	}

	/**
	 * Create meme row
	 *
	 * @param messageId Meme message ID
	 */
	fun createMeme(messageId: Snowflake) {
		transaction {
			Meme.new(messageId.value.toLong()) {}
		}
	}

	/**
	 * Get meme max score
	 *
	 * @param messageId Meme message ID
	 */
	fun memeMaxScore(messageId: Snowflake) = transaction {
		Meme.findById(messageId.value.toLong())?.maxScore
	}

	/**
	 * Add score to meme
	 *
	 * @param messageId Meme message ID
	 */
	fun setMemeScore(messageId: Snowflake, score: Int) = transaction {
		Meme.new(messageId.value.toLong()) {
			maxScore = score
		}
	}
}