package bot.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.member
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import dev.kord.common.entity.ButtonStyle
import dev.kord.rest.builder.message.embed
import kotlin.time.Duration.Companion.seconds

class Fun : Extension() {
	override val name = "fun"
	override val bundle = "cs_dsbot"

	override suspend fun setup() {

		publicSlashCommand(::RollArgs) {
			name = "extensions.fun.roll.commandName"
			description = "extensions.fun.roll.commandDescription"

			action {
				val number = (arguments.min..arguments.max).random()

				respond { embed { title = "🎲 $number" } }
			}
		}

		publicSlashCommand(::DuelArgs) {
			name = "extensions.fun.duel.commandName"
			description = "extensions.fun.duel.commandDescription"

			action duelCommand@ {
				val userName = member!!.asMember().effectiveName

				if (arguments.member.isBot) {
					respond {
						embed {
							title = translate("extensions.fun.duel.waiting.embed.title", arrayOf(userName))
							description = translate("extensions.fun.duel.waiting.robots.embed.description")
						}
					}

					return@duelCommand
				}

				if (member == arguments.member) {
					respond {
						embed {
							title = translate("extensions.fun.duel.suicided.embed.title", arrayOf(userName))
							description = translate("extensions.fun.duel.suicided.embed.description", arrayOf(member!!.mention))
						}
					}

					return@duelCommand
				}

				respond {
					content = arguments.member.mention

					embed {
						title = translate("extensions.fun.duel.waiting.embed.title", arrayOf(userName))
						description = translate(
							"extensions.fun.duel.waiting.embed.description",
							arrayOf(arguments.member.mention)
						)
					}

					components(60.seconds) {
						publicButton {
							bundle = this@Fun.bundle

							label = translate("extensions.fun.duel.waiting.buttons.accept")
							style = ButtonStyle.Success

							check { failIf(event.interaction.user != arguments.member.asUser()) }

							action {
								val duelists = listOf(this@duelCommand.member!!, arguments.member)
								val winner = duelists.random()
								val looser = duelists.find { it != winner }!!

								edit {
									embed {
										title = translate("extensions.fun.duel.accepted")
										description = "${looser.mention} 🔫 ${winner.mention}"
									}

									components { }
								}

								timeoutTask?.cancel()
							}
						}

						publicButton {
							bundle = this@Fun.bundle

							label = translate("extensions.fun.duel.waiting.buttons.refuse")
							style = ButtonStyle.Danger

							check { failIf(event.interaction.user != arguments.member.asUser()) }

							action {
								edit {
									embed {
										title = translate("extensions.fun.duel.refused")
										description = translate(
											"extensions.fun.duel.refused.description",
											arrayOf(arguments.member.mention)
										)
									}

									components { }
								}

								timeoutTask?.cancel()
							}
						}

						onTimeout {
							edit {
								embed {
									title = translate("extensions.fun.duel.timedOut.embed.title")
									description = translate(
										"extensions.fun.duel.timedOut.embed.description",
										arrayOf(arguments.member.mention, this@duelCommand.user.mention)
									)
								}

								components { }
							}
						}
					}
				}
			}
		}
	}

	inner class RollArgs : Arguments() {
		val max by defaultingInt {
			name = "max"
			description = "extensions.fun.roll.arguments.max"
			defaultValue = 100
		}

		val min by defaultingInt {
			name = "min"
			description = "extensions.fun.roll.arguments.min"
			defaultValue = 1
		}
	}

	inner class DuelArgs : Arguments() {
		val member by member {
			name = "member"
			description = "extensions.fun.duel.arguments.member"
		}
	}
}