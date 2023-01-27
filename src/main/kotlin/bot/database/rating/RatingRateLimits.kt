package bot.database.rating

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object RatingRateLimits : LongIdTable("rating_rate_limits") {
	val from = long("from")
	val to = long("to").nullable()
	val expire = timestamp("expire")
}