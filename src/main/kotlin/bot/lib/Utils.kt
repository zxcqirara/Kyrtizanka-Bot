package bot.lib

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.pow

object Utils {
	fun parseTime(instant: Instant, timeZone: TimeZone): String = instant
		.toLocalDateTime(timeZone).toJavaLocalDateTime()
		.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM))

	fun xpForLevel(level: Int) = (160 * (level - 1).toDouble().pow(2) + 180 * (level - 1) + 100).toInt()
	fun levelOfXp(xp: Long): Int {
		var iLevel = 0
		var iXp: Int

		while (true) {
			iLevel += 1
			iXp = xpForLevel(iLevel)

			if (xp < iXp) break
		}

		return iLevel - 1
	}
}