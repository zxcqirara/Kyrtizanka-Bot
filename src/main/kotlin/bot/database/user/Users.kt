package bot.database.user

import org.jetbrains.exposed.dao.id.LongIdTable

object Users : LongIdTable(columnName = "user_id") {
	val experience = long("experience").default(0)
	val voiceTime = long("voice_time").default(0)
	val level = integer("level").default(0)
	val specialAccess = bool("special_access").default(false)
	val useEmoji = bool("use_emoji").default(true)
	val socialRating = integer("social_rating").default(10)
	val socialBlackList = bool("social_black_list").default(false)
}