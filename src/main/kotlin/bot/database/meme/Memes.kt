package bot.database.meme

import org.jetbrains.exposed.dao.id.LongIdTable

object Memes : LongIdTable(columnName = "message_id") {
	val maxScore = integer("max_score")
}