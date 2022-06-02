package bot.tables

import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.BaseTable
import org.ktorm.schema.int
import org.ktorm.schema.long

object MemesScoreTable : BaseTable<MemeRow>("memes_score", schema = "bot") {
	val messageId = long("message_id")
	val maxScore = int("max_score")

	override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = MemeRow(
		row[messageId]!!,
		row[maxScore]!!
	)
}

data class MemeRow(
	val messageId: Long,
	val maxScore: Int
)