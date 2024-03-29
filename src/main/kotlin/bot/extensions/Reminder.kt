package bot.extensions

import bot.lib.Config
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.duration
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import com.kotlindiscord.kord.extensions.utils.toDuration
import dev.kord.common.Color
import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.toMessageFormat
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.embed
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.time.ExperimentalTime

@ExperimentalTime
class Reminder : Extension() {
	override val name = "reminder"
	override val bundle = "cs_dsbot"

	private val reminders = mutableListOf<Remind>()

	override suspend fun setup() {
		ephemeralSlashCommand {
			name = "extensions.reminder.commandName"
			description = "extensions.reminder.commandDescription"

			ephemeralSubCommand(::CreateArgs) {
				name = "extensions.reminder.create.commandName"
				description = "extensions.reminder.create.commandDescription"

				action {
					val text = arguments.text
					val duration = arguments.duration.toDuration(TimeZone.of(Config.discord.timeZone))

					respond { embed {
						title = translate("extensions.reminder.create.embed.title")
						description = text
						color = Color(0x1C7ED6)
					} }

					lateinit var task: Task
					var reminder: Remind? = null

					task = Scheduler().schedule(duration) {
						channel.createMessage {
							content = user.mention

							embed {
								title = translate("extensions.reminder.remind")
								description = text
								color = Color(0x1C7ED6)
							}
						}

						reminders.remove(reminder)
					}

					reminder = Remind(user, text, task, event.interaction.id.timestamp + duration)
					reminders += reminder
				}
			}

			ephemeralSubCommand {
				name = "extensions.reminder.list.commandName"
				description = "extensions.reminder.list.commandDescription"

				action {
					val reminders = reminders.filter { it.user == user }

					respond {
						embed {
							if (reminders.isNotEmpty()) {
								title = translate("extensions.reminder.list.embed.title")
								description = buildString {
									reminders.forEach { remind ->
										val index = reminders.indexOf(remind)
										val timestamp = remind.endsTime.toMessageFormat(DiscordTimestampStyle.ShortDateTime)
										val text = getShort(remind.text)

										appendLine("#$index $timestamp `$text`")
									}
								}
							}
							else {
								title = translate("extensions.reminder.errors.noReminds")
							}

							color = Color(0x1C7ED6)
						}
					}
				}
			}

			ephemeralSubCommand(::RemoveArgs) {
				name = "extensions.reminder.remove.commandName"
				description = "extensions.reminder.remove.commandDescription"

				action {
					val remind = reminders.getOrNull(arguments.id)

					if (remind == null) {
						respond { embed {
							title = translate("extensions.reminder.errors.cantFind")
							color = Color(0xFF0000)
						} }
						return@action
					}

					if (user != remind.user) {
						respond { embed {
							title = translate("extensions.reminder.errors.notYourRemind")
							color = Color(0xFF0000)
						} }
						return@action
					}

					remind.task.cancel()
					reminders.remove(remind)

					respond {
						embed {
							title = translate("extensions.reminder.remove.embed.title", arrayOf(arguments.id))
							color = Color(0x1C7ED6)
						}
					}
				}
			}

			ephemeralSubCommand(::EditArgs) {
				name = "extensions.reminder.edit.commandName"
				description = "extensions.reminder.edit.commandDescription"

				action {
					val reminder = reminders.getOrNull(arguments.id)

					if (reminder == null) {
						respond { embed {
							title = translate("extensions.reminder.errors.cantFind")
							color = Color(0xFF0000)
						} }
						return@action
					}

					if (user != reminder.user) {
						respond { embed {
							title = translate("extensions.reminder.errors.notYourRemind")
							color = Color(0xFF0000)
						} }
						return@action
					}

					reminder.text = arguments.newText

					respond {
						embed {
							title = translate("extensions.reminder.edit.embed.title", arrayOf(arguments.id))
							description = arguments.newText
							color = Color(0x1C7ED6)
						}
					}
				}
			}
		}
	}

	inner class CreateArgs : Arguments() {
		val duration by duration {
			name = "duration"
			description = "extensions.reminder.create.arguments.duration"
		}
		val text by string {
			name = "text"
			description = "extensions.reminder.create.arguments.text"
		}
	}

	inner class RemoveArgs : Arguments() {
		val id by int {
			name = "id"
			description = "extensions.reminder.arguments.id"
		}
	}

	inner class EditArgs : Arguments() {
		val id by int {
			name = "id"
			description = "extensions.reminder.arguments.id"
		}
		val newText by string {
			name = "text"
			description = "extensions.reminder.edit.arguments.newText"
		}
	}

	private fun getShort(text: String): String {
		val len = text.length

		return if (len > 30) {
			when (len - 30) {
				1 -> "$text."
				2 -> "$text.."
				else -> "$text..."
			}
		} else text
	}

	data class Remind(
		val user: UserBehavior,
		var text: String,
		val task: Task,
		val endsTime: Instant
	)
}