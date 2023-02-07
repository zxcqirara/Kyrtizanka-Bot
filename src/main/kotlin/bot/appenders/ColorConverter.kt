package bot.appenders

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.pattern.color.HighlightingCompositeConverter
import ch.qos.logback.classic.spi.ILoggingEvent

class ColorConverter : HighlightingCompositeConverter() {
	override fun convert(event: ILoggingEvent): String {
		val code = when (event.level.toInt()) {
			Level.ERROR_INT -> "31mE"
			Level.WARN_INT -> "33mW"
			Level.INFO_INT -> "32mI"
			Level.DEBUG_INT -> "34mD"
			Level.TRACE_INT -> "35mT"
			else -> "37mU"
		}

		return "\u001B[$code\u001B[0m"
	}
}