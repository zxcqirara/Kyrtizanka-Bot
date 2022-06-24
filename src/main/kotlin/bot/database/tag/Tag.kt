package bot.database.tag

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Tag(id: EntityID<Int>) : IntEntity(id) {
	companion object : IntEntityClass<Tag>(Tags)

	var name by Tags.name
	var text by Tags.text
	var attachments by Tags.attachments
	var addedBy by Tags.addedBy
}