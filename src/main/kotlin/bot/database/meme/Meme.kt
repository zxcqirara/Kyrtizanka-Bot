package bot.database.meme

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Meme(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Meme>(Memes)

	var maxScore by Memes.maxScore
}