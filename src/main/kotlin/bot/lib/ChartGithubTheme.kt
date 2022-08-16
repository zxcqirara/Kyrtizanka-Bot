package bot.lib

import org.knowm.xchart.style.Styler
import org.knowm.xchart.style.theme.AbstractBaseTheme
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font

class ChartGithubTheme : AbstractBaseTheme() {
	override fun getChartBackgroundColor() = Color(0x161B22)
	override fun getPlotBackgroundColor() = Color(0x0D1117)
	override fun getLegendBackgroundColor() = chartBackgroundColor
	override fun getChartTitleBoxBackgroundColor() = chartBackgroundColor
	override fun getChartTitleBoxBorderColor() = Color(0x0000, true)

	override fun getPlotBorderColor() = chartTitleBoxBorderColor // Color(0x6E7681)
	override fun getLegendBorderColor() = plotBorderColor
	override fun getAxisTickMarksColor() = chartTitleBoxBorderColor
	override fun getPlotGridLinesColor() = Color(0x556E7681, true)

	override fun getAnnotationTextFontColor() = Color(0xC9D1D9)
	override fun getAnnotationTextPanelFontColor() = annotationTextFontColor
	override fun getChartFontColor() = annotationTextFontColor
	override fun getAxisTickLabelsColor() = annotationTextFontColor
	override fun getPlotGridLinesStroke() = BasicStroke(.5f)

	override fun getLegendPosition() = Styler.LegendPosition.InsideNW
	override fun isLegendVisible() = false

	override fun getSeriesColors() = arrayOf(
		Color(0x79C0FF),
		Color(0xD2A8FF),
		Color(0xFF7B72),
		Color(0xC9D1D9)
	)

	override fun getBaseFont() = Font("Dialog", Font.PLAIN, 10)
}