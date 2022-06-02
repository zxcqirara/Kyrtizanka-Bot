package bot.tables

import bot.extensions.SocialRating
import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.*

object UsersTable : BaseTable<UserRow>("users", schema = "bot") {
	val userId = long("user_id")
	val experience = long("experience")
	val voiceTime = long("voice_time")
	val level = int("level")
	val specialAccess = boolean("special_access")
	val useEmoji = boolean("use_emoji")
	val socialRating = int("social_rating")

	override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = UserRow(
		row[userId]!!,
		row[experience]!!,
		row[voiceTime]!!,
		row[level]!!,
		row[specialAccess]!!,
		row[useEmoji]!!,
		row[socialRating]!!
	)
}

data class UserRow(
	val userId: Long,
	val experience: Long,
	val voiceTime: Long,
	val level: Int,
	val specialAccess: Boolean,
	val useEmoji: Boolean,
	val socialRating: Int
)