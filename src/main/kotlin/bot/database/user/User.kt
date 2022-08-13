package bot.database.user

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class User(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<User>(Users)

	var experience by Users.experience
	var voiceTime by Users.voiceTime
	var level by Users.level
	var specialAccess by Users.specialAccess
	var useEmoji by Users.useEmoji
	var socialRating by Users.socialRating
	var socialBlackList by Users.socialBlackList
}