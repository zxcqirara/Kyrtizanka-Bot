package bot.database.tag

import bot.lib.array
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.TextColumnType

object Tag : IntIdTable() {
	val text = varchar("text", 4000)
	val images = array<String>("images", TextColumnType())
}