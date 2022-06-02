package bot.tables

import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.BaseTable
import org.ktorm.schema.long
import org.ktorm.schema.timestamp
import java.time.Instant

object RatingTable : BaseTable<RatingRow>("rating", schema = "bot") {
	val from = long("from")
	val to = long("to")
	val expire = timestamp("expire")

	override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = RatingRow(
		row[from]!!,
		row[to]!!,
		row[expire]!!
	)
}

data class RatingRow(
	val from: Long,
	val to: Long,
	val expire: Instant
)