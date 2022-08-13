package bot.database.rep_messages

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class RepMessage(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<RepMessage>(RepMessages)

	var from by RepMessages.from
	var to by RepMessages.to
	var isPlus by RepMessages.isPlus
}