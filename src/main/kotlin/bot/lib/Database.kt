package bot.lib

import bot.readConfig
import bot.tables.ExperienceTable
import bot.tables.MemesScoreTable
import bot.tables.UserRow
import bot.tables.UsersTable
import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.ktorm.support.postgresql.insertOrUpdate
import kotlin.math.roundToLong
import org.ktorm.database.Database as KtDatabase

object Database {
	lateinit var database: KtDatabase

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
	): XpUpdateEvent {
		val nowHas = database.sequenceOf(UsersTable)
			.find { it.userId eq userId }

		if (nowHas == null) {
			val xpEvent = XpUpdateEvent(count.toLong(), 0)
			bot.send(xpEvent)

			database.insert(UsersTable) {
				set(it.userId, userId)
				set(it.experience, count.toLong())

				if (xpEvent.needUpdate)
					set(it.level, xpEvent.newLevel)
			}

			database.insert(ExperienceTable) {
				set(it.userId, userId)
				set(it.count, count)
				set(it.time, moment.toJavaInstant())
				set(it.multiplier, 1f)
			}

			return xpEvent
		}
		else {
			val multiplied = (if (nowHas.socialRating > 0)
				count * nowHas.socialRating
			else
				count / nowHas.socialRating).toShort()

			val newExp = nowHas.experience + multiplied

			val xpEvent = XpUpdateEvent(newExp, nowHas.level)
			bot.send(xpEvent)

			database.update(UsersTable) {
				set(it.experience, newExp)

				if (xpEvent.needUpdate)
					set(it.level, xpEvent.newLevel)

				where { it.userId eq userId }
			}

			database.update(ExperienceTable) {
				set(it.count, count)
				set(it.time, moment.toJavaInstant())
				set(it.multiplier, nowHas.socialRating / 10f)

				where { it.userId eq userId }
			}

			return xpEvent
		}
	}

	/**
	 * Get user by ID
	 *
	 * @param userId User ID
	 */
	fun getUser(userId: Snowflake): UserRow {
		val longUserId = userId.value.toLong()

		var data = database.sequenceOf(UsersTable)
			.find { it.userId eq longUserId }

		if (data == null) {
			database.insert(UsersTable) { set(it.userId, longUserId) }

			data = database.sequenceOf(UsersTable)
				.find { it.userId eq longUserId }!!
		}

		return data
	}

	/**
	 * Clean up database
	 */
	fun cleanUp() {
		database.deleteAll(UsersTable)
		database.deleteAll(ExperienceTable)
	}

	/**
	 * Get top users
	 *
	 * @param count Count of users will be returned from top
	 */
	fun getTopUsers(count: Int): List<UserRow> {
		return database.sequenceOf(UsersTable)
			.sortedByDescending { it.experience }
			.take(count).toList()
	}

	/**
	 * Add voice seconds to user
	 *
	 * @param userId User ID
	 * @param count Count of seconds will be added
	 */
	suspend fun addSeconds(userId: Snowflake, count: Long) {
		val userIdLong = userId.value.toLong()
		val perSecond = readConfig().experience.perSecond

		database.update(UsersTable) {
			set(it.voiceTime, it.voiceTime + count)
			set(it.experience, it.experience + (count * perSecond).roundToLong())

			where { it.userId eq userIdLong }
		}
	}

	/**
	 * Check have user special accesss
	 *
	 * @param kord Kord instance (need for check)
	 * @param userId User ID
	 */
	suspend fun hasSpecialAccess(kord: Kord, userId: Snowflake): Boolean {
		val user = database.sequenceOf(UsersTable)
			.find { it.userId eq userId.value.toLong() }

		return user!!.specialAccess || kord.getApplicationInfo().ownerId == userId
	}

	/**
	 * Give special access to user
	 *
	 * @param userId User ID
	 */
	fun giveSpecialAccess(userId: Snowflake) {
		database.insertOrUpdate(UsersTable) {
			set(it.userId, userId.value.toLong())
			set(it.specialAccess, true)

			onConflict { set(it.specialAccess, true) }
		}
	}

	/**
	 * Take special access from user
	 *
	 * @param userId User ID
	 */
	fun takeSpecialAccess(userId: Snowflake) {
		database.update(UsersTable) {
			set(it.specialAccess, false)

			where { it.userId eq userId.value.toLong() }
		}
	}

	/**
	 * Get list of users with special access
	 */
	fun getUsersWithSpecialAcess() =
		database.sequenceOf(UsersTable)
			.filter { it.specialAccess eq true }

	/**
	 * Returns need use emoji for user
	 */
	fun useEmoji(userId: Snowflake) =
		database.sequenceOf(UsersTable)
			.find { it.userId eq userId.value.toLong() }
			?.useEmoji ?: true

	/**
	 * Add rating to user
	 *
	 * @param userId User ID
	 */
	fun addRating(userId: Snowflake) {
		val idLong = userId.value.toLong()
		val current = database.sequenceOf(UsersTable)
			.find { it.userId eq idLong }

		if (current == null) {
			database.insert(UsersTable) {
				set(it.userId, idLong)
				set(it.socialRating, 11)
			}
		}
		else {
			val newRating = if (current.socialRating == -2) 1 else current.socialRating + 1

			database.update(UsersTable) {
				set(it.socialRating, newRating)

				where { it.userId eq idLong }
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
		val current = database.sequenceOf(UsersTable)
			.find { it.userId eq idLong }

		if (current == null) {
			database.insert(UsersTable) {
				set(it.userId, idLong)
				set(it.socialRating, 9)
			}
		}
		else {
			val newRating = if (current.socialRating == 1) -2 else current.socialRating - 1

			database.update(UsersTable) {
				set(it.socialRating, newRating)

				where { it.userId eq idLong }
			}
		}
	}

	/**
	 * Create meme row
	 *
	 * @param messageId Meme message ID
	 */
	fun createMeme(messageId: Snowflake) {
		database.insert(MemesScoreTable) {
			set(it.messageId, messageId.value.toLong())
		}
	}

	/**
	 * Get meme max score
	 *
	 * @param messageId Meme message ID
	 */
	fun memeMaxScore(messageId: Snowflake) =
		database.sequenceOf(MemesScoreTable)
		.find { it.messageId eq messageId.value.toLong() }?.maxScore

	/**
	 * Add score to meme
	 *
	 * @param messageId Meme message ID
	 */
	fun setMemeScore(messageId: Snowflake, score: Int) {
		database.insert(MemesScoreTable) {
			set(it.messageId, messageId.value.toLong())
			set(it.maxScore, score)
		}
	}
}