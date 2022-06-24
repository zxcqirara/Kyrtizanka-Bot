package bot.database.experience

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Experiences : LongIdTable() {
	val userId = long("user_id")
	val count = short("count")
	val time = timestamp("time")
	val multiplier = float("multiplier").default(1f)
}