package bot.database.rating

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object Ratings : LongIdTable("rating", "from") {
	val to = long("to")
	val expire = timestamp("expire")
}