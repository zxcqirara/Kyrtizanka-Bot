package bot.extensions

import bot.database.experience.Experience
import bot.lib.ChartGithubTheme
import bot.lib.Config
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.rest.NamedFile
import dev.kord.rest.builder.message.modify.embed
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.transactions.transaction
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.markers.SeriesMarkers
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.io.File
import kotlin.io.path.Path

class Statistic : Extension() {
	override val name = "visual-stats"
	override val bundle = "cs_dsbot"

	override suspend fun setup() {
		val timeZone = TimeZone.of(Config.discord.timeZone)

		publicSlashCommand {
			name = "extensions.statistic.commandName"
			description = "extensions.statistic.commandDescription"

			publicSubCommand {
				name = "extensions.statistic.global.commandName"
				description = "extensions.statistic.global.commandDescription"

				action {
					val gray = Color(0x868E96)
					val green = Color(0x40C057)
					val red = Color(0xF03E3E)

					val dbData = transaction {
						Experience.all().sortedBy { it.time }.toList()
					}

					val data = dbData.groupBy { it.time.toLocalDateTime(timeZone).dayOfYear }

					val xData = data.map { it.key }
					val yData = data.map { it.value }.map { value -> value.map { it.count }.sum() }

					val chart = XYChartBuilder()
						.xAxisTitle("День месяца")
						.yAxisTitle("Кол-во опыта")
						.width(400)
						.height(225)
						.build()

					chart.styler.theme = ChartGithubTheme()
					chart.styler.axisTitleFont = Font(Font.DIALOG, Font.PLAIN, 10)
					chart.styler.axisTickLabelsFont = Font(Font.DIALOG, Font.PLAIN, 8)

					yData.forEachIndexed { index, yCurrent ->
						val xCurrent = xData[index]
						val xNext = xData.getOrNull(index + 1) ?: return@forEachIndexed
						val yNext = yData.getOrNull(index + 1) ?: return@forEachIndexed

						val lineSeries = chart.addSeries(
							"line-$index",
							listOf(xCurrent, xNext),
							listOf(yCurrent, yNext)
						)

						lineSeries.lineColor = gray
						lineSeries.lineStyle = BasicStroke(1f)
						lineSeries.marker = SeriesMarkers.NONE
					}

					yData.forEachIndexed { index, yCurrent ->
						val xNext = xData.getOrNull(index + 1) ?: return@forEachIndexed
						val yNext = yData.getOrNull(index + 1) ?: return@forEachIndexed

						val markerSeries = chart.addSeries(
							"marker-$index",
							listOf(xNext),
							listOf(yNext)
						)

						markerSeries.lineColor = gray
						markerSeries.lineStyle = BasicStroke(1f)

						println("$yCurrent > $yNext")
						if (yNext > yCurrent) {
							markerSeries.marker = SeriesMarkers.TRIANGLE_UP
							markerSeries.markerColor = green
						}
						else if (yNext < yCurrent) {
							markerSeries.marker = SeriesMarkers.TRIANGLE_DOWN
							markerSeries.markerColor = red
						}
						else {
							markerSeries.marker = SeriesMarkers.DIAMOND
						}
					}

					val path = "${event.interaction.id.value}.png"

					BitmapEncoder.saveBitmapWithDPI(chart, path, BitmapFormat.PNG, 300)

					lateinit var chartFile: NamedFile
					val message = respond { chartFile = addFile(Path(path)) }

					File(path).delete()

					message.edit {
						embed {
							title = "Статистика опыта"

							image = chartFile.url
						}
					}
				}
			}
		}
	}
}