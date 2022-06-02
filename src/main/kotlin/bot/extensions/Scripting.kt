package bot.extensions


import bot.lib.Database
import bot.tables.UsersTable
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.chat.ChatCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescingString
import com.kotlindiscord.kord.extensions.commands.converters.impl.user
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.extensions.chatGroupCommand
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.Color
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.create.embed
import org.koin.core.component.inject
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import javax.script.ScriptEngineManager

class Scripting : Extension() {
	override val name = "scripting"
	private val translationsProvider: TranslationsProvider by inject()

	override suspend fun setup() {
		suspend fun evalCmd(chatCommandContext: ChatCommandContext<out FastrunArgs>) {
			chatCommandContext.apply {
				var cleanScript = arguments.script
				if (cleanScript.startsWith("```"))
					cleanScript = cleanScript.replaceFirst("```", "")
				if (cleanScript.startsWith("```kt\n"))
					cleanScript = cleanScript.replaceFirst("```kt\n", "")
				if (cleanScript.endsWith("```"))
					cleanScript.dropLast(3)

				val engine = ScriptEngineManager().getEngineByExtension("kts")!!
				engine.put("event", event)
				engine.put("message", message)
				engine.put("engine", engine)
				engine.put("kord", this@Scripting.kord)

				runCatching {
					engine.eval(cleanScript)
				}
					.onSuccess {
						message.reply { content = "$it" }
					}
					.onFailure {
						message.reply { content = "**FAIL**\n```${it.message}```\n```${it::class.qualifiedName}```" }
					}
			}
		}

		suspend fun userCheck(context: CheckContext<MessageCreateEvent>) {
			val hasAccess = Database.hasSpecialAccess(kord, context.event.member!!.id)

			context.failIf(!hasAccess)
		}

		chatGroupCommand {
			name = "extensions.scripting.commandName"
			description = "extensions.scripting.commandDescription"

			check(::userCheck)

			chatCommand(::FastrunArgs) {
				name = "extensions.scripting.fastrun.commandName"
				description = "extensions.scripting.fastrun.commandDescription"

				action {
					evalCmd(this)
				}
			}

			chatGroupCommand {
				name = "extensions.scripting.allowed.commandName"
				description = "extensions.scripting.allowed.commandDescription"

				check { userCheck(this) }

				chatCommand(::AllowedAddArgs) {
					name = "extensions.scripting.allowed.add.commandName"
					description = "extensions.scripting.allowed.add.commandDescription"

					action {
						val user = arguments.user

						Database.giveSpecialAccess(user.id)

						message.reply { embed {
							title = translate("extensions.scripting.allowed.add.embed.title")
							description = translate("extensions.scripting.allowed.add.embed.description", )
							color = Color(0x0000CC)
						} }
					}
				}

				chatCommand(::AllowedRemoveArgs) {
					name = "extensions.scripting.allowed.remove.commandName"
					description = "extensions.scripting.allowed.remove.commandDescription"

					action {
						val user = arguments.user

						Database.takeSpecialAccess(user.id)

						message.reply { embed {
							title = translate("extensions.scripting.allowed.remove.embed.title")
							description = translate(
								"extensions.scripting.allowed.remove.embed.description",
								arrayOf(user.mention)
							)
							color = Color(0x0000CC)
						} }
					}
				}

				chatCommand {
					name = "extensions.scripting.allowed.list.commandName"
					description = "extensions.scripting.allowed.list.commandDescription"

					action {
						val usersList = Database.getUsersWithSpecialAcess()
							.joinToString("\n") { "<@${it.userId}>" }

						message.reply { embed {
							title = translate("extensions.scripting.allowed.list.embed.title")
							description = usersList
						} }
					}
				}
			}
		}

		chatCommand(::FastrunArgs) {
			name = "extensions.scripting.eval.commandName"
			description = "extensions.scripting.eval.commandDescription"

			check(::userCheck)

			action {
				evalCmd(this)
			}
		}
	}

	inner class FastrunArgs : Arguments() {
		val script by coalescingString {
			name = "script"
			description = translationsProvider.translate("extensions.scripting.fastrun.arguments.script")
		}
	}

	inner class AllowedAddArgs : Arguments() {
		val user by user {
			name = "user"
			description = translationsProvider.translate("extensions.scripting.allowed.add.arguments.user")
		}
	}
	inner class AllowedRemoveArgs : Arguments() {
		val user by user {
			name = "user"
			description = translationsProvider.translate("extensions.scripting.allowed.remove.arguments.user")
		}
	}
}