package bot.database.rating

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class RatingRateLimit(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<RatingRateLimit>(RatingRateLimits)

	var from by RatingRateLimits.from
	var to by RatingRateLimits.to
	var expire by RatingRateLimits.expire
}