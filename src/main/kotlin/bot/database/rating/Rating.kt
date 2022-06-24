package bot.database.rating

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Rating(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Rating>(Ratings)

	var to by Ratings.to
	var expire by Ratings.expire
}