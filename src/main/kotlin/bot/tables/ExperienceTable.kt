package bot.tables

import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.*
import java.time.Instant

object ExperienceTable : BaseTable<ExperienceRow>("experience", schema = "bot") {
	val userId = long("user_id")
	val count = short("count")
	val time = timestamp("time")
	val multiplier = float("multiplier")

	override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = ExperienceRow(
		row[userId]!!,
		row[count]!!,
		row[time]!!,
		row[multiplier]!!
	)
}

data class ExperienceRow(
	val userId: Long,
	val count: Short,
	val time: Instant,
	val multiplier: Float
)