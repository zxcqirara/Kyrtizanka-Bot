package bot.database.tag

import bot.lib.array
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.TextColumnType

object Tags : IntIdTable() {
	val name = varchar("name", 30).uniqueIndex()
	val text = varchar("text", 4000)
	val attachments = array<String>("images", TextColumnType()).default(emptyArray())
	val addedBy = long("added_by")
}