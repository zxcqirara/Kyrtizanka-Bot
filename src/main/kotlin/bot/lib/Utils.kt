package bot.lib

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.regex.Pattern
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object Utils {
	fun parseToDuration(text: String): Duration? {
		val regex = "^((\\d+)y)?((\\d+)M)?((\\d+)w)?((\\d+)d)?((\\d+)h)?((\\d+)m)?((\\d+)s)?$"
		val pattern = Pattern.compile(regex, Pattern.MULTILINE)
		val matcher = pattern.matcher(text)
		var duration = Duration.ZERO

		if (!matcher.find()) return duration

		fun getByGroup(number: Int) = (matcher.group(number) ?: "0").toLongOrNull() ?: 0

		// Years
		duration += getByGroup(2).days * 365.2425
		// Months
		duration += getByGroup(4).days * 365.2425 / 12
		// Weeks
		duration += getByGroup(6).days * 7
		// Days
		duration += getByGroup(8).days
		// Hours
		duration += getByGroup(10).hours
		// Minutes
		duration += getByGroup(12).minutes
		// Seconds
		duration += getByGroup(14).seconds

		return if (duration > Duration.ZERO) duration else null
	}

	fun parseTime(instant: Instant): String = instant
		.toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime()
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