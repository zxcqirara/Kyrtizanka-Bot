package bot.database.rep_messages

import org.jetbrains.exposed.dao.id.LongIdTable

object RepMessages : LongIdTable("rep_messages", columnName = "message_id") {
	val from = long("from")
	val to = long("to")
	val isPlus = bool("is_plus")
}