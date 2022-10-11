package bot.extensions

import bot.database.tag.Tag
import bot.database.tag.Tags
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescingString
import com.kotlindiscord.kord.extensions.extensions.*
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.addReaction
import com.kotlindiscord.kord.extensions.utils.hasPermission
import com.kotlindiscord.kord.extensions.utils.isNullOrBot
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import io.ktor.client.request.forms.*
import io.ktor.utils.io.jvm.javaio.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URL

class Tags : Extension() {
	override val name = "tags"
	override val bundle = "cs_dsbot"

	override suspend fun setup() {
		event<MessageCreateEvent> {
			check {
				failIf(event.message.author.isNullOrBot())
				failIf(!event.message.content.startsWith("#"))
			}

			action {
				val tagName = event.message.content.drop(1)
				val tagData = transaction {
					Tag.find { Tags.name eq tagName.lowercase() }.firstOrNull()
				} ?: return@action

				event.message.reply {
					content = tagData.text

					tagData.attachments.forEach { imageLink ->
						val stream = URL(imageLink).openStream()
						val ext = imageLink.substringAfterLast(".")
						val provider = ChannelProvider { stream.toByteReadChannel() }

						addFile("${tagData.attachments.indexOf(imageLink)}.$ext", provider)
					}
				}
			}
		}

		chatCommand(::TagArgs) {
			name = "extensions.tags.tag.commandName"
			description = "extensions.tags.tag.commandDescription"

			check {
				failIfNot(
					event.member!!.hasPermission(Permission.ManageGuild) ||
						event.member!!.hasPermission(Permission.Administrator) ||
						event.getGuild()!!.ownerId == event.member!!.id
				)
			}

			action {
				val message = event.message
				val ref = message.referencedMessage
					?: return@action run { message.reply { content = translate("extensions.errors.nonReferenced") } }

				val isTagExists = transaction {
					Tag.find { Tags.name eq arguments.name }.firstOrNull() != null
				}

				if (isTagExists)
					return@action run { message.reply { content = translate("extensions.tags.tag.errors.alreadyExists") } }

				newSuspendedTransaction {
					Tag.new {
						name = arguments.name
						text = ref.content
						attachments = ref.attachments.map { it.proxyUrl }.toTypedArray()
						addedBy = message.author!!.id.value.toLong()
					}
				}

				event.message.addReaction("✅")
			}
		}

		ephemeralSlashCommand {
			name = "extensions.tags.commandName"
			description = "extensions.tags.commandDescription"

			ephemeralSubCommand {
				name = "extensions.tags.list.commandName"
				description = "extensions.tags.list.commandDescription"

				action {
					val list = transaction {
						Tag.all().toList()
					}.joinToString("\n") { "#${it.name}" }
						.takeIf { it.isNotEmpty() } ?: translate("extensions.tags.list.errors.noTags")

					respond { content = list }
				}
			}

			ephemeralSubCommand(::TagArgs) {
				name = "extensions.tags.remove.commandName"
				description = "extensions.tags.remove.commandDescription"

				action {
					val tag = transaction {
						Tag.find { Tags.name eq arguments.name }.firstOrNull()
					} ?: return@action run { respond { content = translate("extensions.tags.remove.errors.invalidTag") } }

					if (tag.addedBy != member!!.id.value.toLong())
						return@action run { respond { content = translate("extensions.tags.remove.errors.notYourTag") } }

					transaction { tag.delete() }
					respond { content = translate("extensions.tags.remove.success") }
				}
			}
		}
	}

	inner class TagArgs : Arguments() {
		val name by coalescingString {
			name = "name"
			description = "Name of your tag"
		}
	}
}