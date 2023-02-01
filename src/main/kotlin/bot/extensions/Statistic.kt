package bot.extensions

import bot.database.experience.Experience
import bot.database.experience.Experiences
import bot.lib.ChartGithubTheme
import bot.lib.Config
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalMember
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.transactions.transaction
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.markers.SeriesMarkers
import org.koin.core.component.inject
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.io.File
import kotlin.io.path.Path

class Statistic : Extension() {
	override val name = "visual-stats"
	override val bundle = "cs_dsbot"

	private val translationsProvider: TranslationsProvider by inject()
	private val timeZone = TimeZone.of(Config.discord.timeZone)

	override suspend fun setup() {

		publicSlashCommand {
			name = "extensions.statistic.commandName"
			description = "extensions.statistic.commandDescription"

			publicSubCommand {
				name = "extensions.statistic.global.commandName"
				description = "extensions.statistic.global.commandDescription"

				action {
					val dbData = transaction {
						Experience
							.all()
							.sortedBy { it.time }
							.toList()
					}

					procedureGraphSend(this, dbData)
				}
			}

			publicSubCommand(::TargetArgs) {
				name = "extensions.statistic.user.commandName"
				description = "extensions.statistic.user.commandDescription"

				action {
					val targetId = (arguments.target ?: member!!).id.value.toLong()

					val dbData = transaction {
						Experience
							.find { Experiences.userId eq targetId }
							.sortedBy { it.time }
							.toList()
					}

					procedureGraphSend(this, dbData)
				}
			}
		}
	}

	private suspend fun procedureGraphSend(context: PublicSlashCommandContext<*, ModalForm>, dbData: List<Experience>) {
		val gray = Color(0x868E96)
		val green = Color(0x40C057)
		val red = Color(0xF03E3E)

		val data = dbData.groupBy { it.time.toLocalDateTime(timeZone).dayOfYear }

		val xData = data.map { it.key }
		val yData = data.map { it.value }.map { value -> value.map { it.count }.sum() }

		val chart = XYChartBuilder()
			.xAxisTitle(translationsProvider.translate("extensions.statistic.embed.image.dayOfMonth", bundleName = bundle))
			.yAxisTitle(translationsProvider.translate("extensions.statistic.embed.image.experienceCount", bundleName = bundle))
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

		val fileName = "${context.event.interaction.id.value}.png"

		BitmapEncoder.saveBitmapWithDPI(chart, fileName, BitmapFormat.PNG, 300)

		context.respond {
			val file = addFile(Path(fileName))

			embed {
				title = translationsProvider.translate("extensions.statistic.embed.title", bundleName = bundle)

				image = file.url
			}
		}

		File(fileName).delete()
	}

	inner class TargetArgs : Arguments() {
		val target by optionalMember {
			name = "user"
			description = translationsProvider.translate("extensions.statistic.user.arguments.target")
		}
	}
}