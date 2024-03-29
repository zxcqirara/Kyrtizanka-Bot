package bot.extensions

import bot.database.experience.Experience
import bot.database.experience.Experiences
import bot.lib.ChartGithubTheme
import bot.lib.Config
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.defaultingEnumChoice
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalMember
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.rest.builder.message.addFile
import dev.kord.rest.builder.message.embed
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.and
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
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class Statistic : Extension() {
	override val name = "visual-stats"
	override val bundle = "cs_dsbot"

	private val translationsProvider: TranslationsProvider by inject()
	private val timeZone = TimeZone.of(Config.discord.timeZone)

	override suspend fun setup() {

		publicSlashCommand {
			name = "extensions.statistic.commandName"
			description = "extensions.statistic.commandDescription"

			publicSubCommand(::TimeArgs) {
				name = "extensions.statistic.global.commandName"
				description = "extensions.statistic.global.commandDescription"

				action {
					val duration = when (arguments.time) {
						TimeChoice.HOUR -> 1.hours
						TimeChoice.DAY -> 1.days
						TimeChoice.MONTH -> 32.days
					}
					val relative = event.interaction.data.id.timestamp + duration

					val dbData = transaction {
						Experience
							.find { Experiences.time lessEq relative }
							.sortedBy { it.time }
							.toList()
					}

					procedureGraphSend(this, dbData, arguments.time)
				}
			}

			publicSubCommand(::TargetNTimeArgs) {
				name = "extensions.statistic.user.commandName"
				description = "extensions.statistic.user.commandDescription"

				action {
					val targetId = (arguments.target ?: member!!).id.value.toLong()
					val duration = when (arguments.time) {
						TimeChoice.HOUR -> 1.hours
						TimeChoice.DAY -> 1.days
						TimeChoice.MONTH -> 32.days
					}
					val relative = event.interaction.data.id.timestamp + duration

					val dbData = transaction {
						Experience
							.find { (Experiences.userId eq targetId) and (Experiences.time lessEq relative) }
							.sortedBy { it.time }
							.toList()
					}

					procedureGraphSend(this, dbData, arguments.time)
				}
			}
		}
	}

	private suspend fun procedureGraphSend(
		context: PublicSlashCommandContext<*, ModalForm>,
		dbData: List<Experience>,
		timeChoice: TimeChoice
	) {
		val gray = Color(0x868E96)
		val green = Color(0x40C057)
		val red = Color(0xF03E3E)

		val statisticData = dbData.groupBy {
			val localTime = it.time.toLocalDateTime(timeZone)

			when (timeChoice) {
				TimeChoice.HOUR -> localTime.minute
				TimeChoice.DAY -> localTime.hour
				TimeChoice.MONTH -> localTime.dayOfMonth
			}
		}

		val xData = statisticData.map { it.key }
		val yData = statisticData.map { it.value }.map { value -> value.map { it.count }.sum() }

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

	inner class TargetNTimeArgs : Arguments() {
		val target by optionalMember {
			name = "user"
			description = "extensions.statistic.user.arguments.target"
		}
		val time by defaultingEnumChoice<TimeChoice> {
			name = "time"
			description = "extensions.statistic.arguments.time"
			defaultValue = TimeChoice.MONTH

			this.typeName = "extensions.statistic.arguments.time.typeName"
			this.bundle = this@Statistic.bundle
		}
	}

	inner class TimeArgs : Arguments() {
		val time by defaultingEnumChoice<TimeChoice> {
			name = "time"
			description = "extensions.statistic.arguments.time"
			defaultValue = TimeChoice.MONTH

			this.typeName = "extensions.statistic.arguments.time.typeName"
			this.bundle = this@Statistic.bundle
		}
	}

	enum class TimeChoice : ChoiceEnum {
		HOUR { override val readableName = "extensions.statistic.arguments.time.hour" },
		DAY { override val readableName = "extensions.statistic.arguments.time.day" },
		MONTH { override val readableName = "extensions.statistic.arguments.time.month" },
	}
}
