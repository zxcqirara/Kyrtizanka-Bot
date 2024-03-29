package bot.lib

import kotlinx.serialization.Serializable

object ConfigDto {
	@Serializable
	data class Discord(
		val token: String = "",
		val guildId: String = "",
		val commandPrefixes: List<String> = listOf(),
		val statsReportChannelId: String = "",
		val memesChannelId: String = "",
		val memeScore: Int = 10,
		val game: String = "Stray",
		val sentryLink: String = "",
		val timeZone: String = "Europe/Moscow",
		val language: String = "ru",
		val experience: Experience = Experience(),
		val privates: Privates = Privates()
	)

	@Serializable
	data class Experience(
		val perCharacter: Float = .5f,
		val perSecond: Float = .05f,
		val deleteTimeout: Float = 3f,
		val doNotAlertBefore: Int = 0,
		val ignore: List<Long> = listOf()
	)

	@Serializable
	data class Database(
		val url: String = "localhost:5432",
		val username: String = "postgres",
		val password: String = "",
		val database: String = "coders_squad"
	)

	@Serializable
	data class Privates(
		val categoryId: String = "",
		val createChannelId: String = ""
	)
}