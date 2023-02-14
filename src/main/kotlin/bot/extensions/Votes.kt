package bot.extensions

import bot.lib.Config
import bot.lib.Utils
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.duration
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralButton
import com.kotlindiscord.kord.extensions.components.types.emoji
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.types.edit
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import com.kotlindiscord.kord.extensions.utils.toDuration
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import kotlinx.datetime.TimeZone
import org.koin.core.component.inject

class Votes : Extension() {
	override val name = "votes"
	override val bundle = "cs_dsbot"

	private val votes = mutableListOf<Vote>()
	private val timeZone = TimeZone.of(Config.discord.timeZone)
	val translatorProvider: TranslationsProvider by inject()

	override suspend fun setup() {
		ephemeralSlashCommand(::Args) {
			name = "extensions.votes.commandName"
			description = "extensions.votes.commandDescription"

			action {
				val duration = arguments.duration.toDuration(TimeZone.of(Config.discord.timeZone))
				val choices = arguments.choices
					.replace(", ", ",")
					.split(",")

				if (choices.size > 23) {
					respond { content = translate("extensions.votes.tooManyChoices") }
					return@action
				}

				val voteStartTime = event.interaction.id.timestamp
				val vote = Vote(arguments.title, choices.map(::Choice), translatorProvider, this@Votes.bundle)
				votes += vote
				val voteIndex = votes.indexOf(vote)

				val voteMessage = channel.createMessage {
					embed {
						title = translate("extensions.votes.inProgress.title")
						description = "__${arguments.title}__\n" + vote.getStatsString()

						timestamp = voteStartTime
					}

					components {
						choices.forEach { label ->
							ephemeralButton {
								bundle = this@Votes.bundle

								this.label = label
								emoji("⬆️")

								action {
									val voteEl = votes[votes.indexOf(vote)]

									voteEl.choices.map { it.votedUsers.remove(user.id) }
									voteEl.choices
										.find { it.name == label }!!
										.votedUsers.add(user.id)

									edit {
										embed {
											title = translate("extensions.votes.inProgress.title")
											description = "__${arguments.title}__\n" + vote.getStatsString()

											timestamp = voteStartTime
										}
									}

									respond { content = translate("extensions.votes.voted") }
								}
							}
						}

						ephemeralButton {
							bundle = this@Votes.bundle

							style = ButtonStyle.Secondary
							label = translate("extensions.votes.seeResults")

							action {
								respond {
									val voteEl = votes[votes.indexOf(vote)]

									content = buildString {
										voteEl.choices.forEach { choice ->
											append("**${choice.name}** ")

											val mentions = choice.votedUsers.map { kord.getUser(it)!!.mention }

											if (mentions.isNotEmpty())
												appendLine(mentions.joinToString(" "))
											else
												appendLine("`${translate("extensions.votes.nobody")}`")
										}
									}
								}
							}
						}

						ephemeralButton {
							bundle = this@Votes.bundle

							style = ButtonStyle.Danger
							label = translate("extensions.votes.forceEnd")

							action {
								val actualVote = votes[voteIndex]

								this.interactionResponse.edit {
									embed {
										title = translate("extensions.votes.ended.title")
										description = buildString {
											appendLine("__${actualVote.title}__")

											actualVote.choices.forEach { choice ->
												appendLine("**${choice.name}** `${choice.votedUsers.size}`")

												val mentionedUsers = choice.votedUsers.map { guild!!.getMember(it).mention }
												if (mentionedUsers.isNotEmpty())
													appendLine(mentionedUsers.joinToString(" "))
												else
													appendLine("`${translate("extensions.votes.nobody")}`")
											}
										}

										footer {
											val startTimestamp = Utils.parseTime(voteStartTime, timeZone)
											val nowTimestamp = Utils.parseTime(event.interaction.id.timestamp, timeZone)

											text = translate("extensions.votes.startedAt", arrayOf(startTimestamp)) + "\n" +
												translate("extensions.votes.endedAt", arrayOf(nowTimestamp))
										}
									}

									components {}
								}

								respond { content = translate("extensions.votes.forceEnded") }
							}
						}
					}
				}

				vote.task = Scheduler().schedule(duration) {
					val voteEl = votes[voteIndex]

					voteMessage.edit {
						embed {
							title = translate("extensions.votes.ended.title")
							description = buildString {
								appendLine("__${voteEl.title}__")

								voteEl.choices.forEach { choice ->
									appendLine("**${choice.name}** `${choice.votedUsers.size}`")

									val mentionedUsers = choice.votedUsers.map { guild!!.getMember(it).mention }
									if (mentionedUsers.isNotEmpty())
										appendLine(mentionedUsers.joinToString(" "))
									else
										appendLine("`${translate("extensions.votes.nobody")}`")
								}
							}

							footer {
								val startTimestamp = Utils.parseTime(voteStartTime, timeZone)
								val nowTimestamp = Utils.parseTime(event.interaction.id.timestamp, timeZone)

								text = translate("extensions.votes.startedAt", arrayOf(startTimestamp)) + "\n" +
									translate("extensions.votes.endedAt", arrayOf(nowTimestamp))
							}
						}

						components {}
					}
				}
			}
		}
	}

	inner class Args: Arguments() {
		val title by string {
			name = "title"
			description = "extensions.votes.arguments.title"
		}
		val choices by string {
			name = "choices"
			description = "extensions.votes.arguments.choices"
		}
		val duration by duration {
			name = "duration"
			description = "extensions.votes.arguments.duration"
		}
	}

	data class Vote(
		val title: String,
		val choices: List<Choice>,
		val translationsProvider: TranslationsProvider,
		val bundle: String,
		var task: Task? = null
	) {
		fun getStatsString() = buildString {
			choices.forEach { choice ->
				appendLine(
					translationsProvider.translate(
						"extensions.votes.votesInfo",
						bundle,
						listOf(choice.name, choice.votedUsers.size)
					)
				)
			}
		}
	}

	data class Choice(
		val name: String,
		val votedUsers: MutableList<Snowflake> = mutableListOf()
	)
}