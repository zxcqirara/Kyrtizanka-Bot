package bot.extensions

import bot.lib.Utils
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.common.Color
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.ZERO
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
					val duration = Utils.parseToDuration(arguments.duration)

					if (duration == null || duration.isNegative() || duration == ZERO) {
						respond { embed {
							title = translate("extensions.errors.unknownDurationFormat")
							color = Color(0xFF0000)
						} }
						return@action
					}

					respond { embed {
						title = translate("extensions.reminder.create.embed.title")
						description = text
						color = Color(0x1C7ED6)
					} }

					val fomattedTime = Utils.parseTime(Clock.System.now() + duration)
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

					reminder = Remind(user, text, task, fomattedTime)
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
										appendLine("#${reminders.indexOf(remind)} **${remind.fomattedTime}** `${getShort(remind.text)}`")
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
		val duration by string {
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
		val fomattedTime: String
	)
}