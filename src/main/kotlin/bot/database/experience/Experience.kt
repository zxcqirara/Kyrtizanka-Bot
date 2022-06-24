package bot.database.experience

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Experience(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Experience>(Experiences)

	var userId by Experiences.userId
	var count by Experiences.count
	var time by Experiences.time
	var multiplier by Experiences.multiplier
}